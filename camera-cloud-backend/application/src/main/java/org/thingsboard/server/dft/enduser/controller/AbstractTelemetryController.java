package org.thingsboard.server.dft.enduser.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.async.DeferredResult;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.common.util.ThingsBoardThreadFactory;
import org.thingsboard.rule.engine.api.msg.DeviceAttributesEventNotificationMsg;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.TenantProfile;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.*;
import org.thingsboard.server.common.data.tenant.profile.DefaultTenantProfileConfiguration;
import org.thingsboard.server.common.transport.adaptor.JsonConverter;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.service.ConstraintValidator;
import org.thingsboard.server.dao.timeseries.TimeseriesService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.AccessValidator;
import org.thingsboard.server.service.security.model.SecurityUser;
import org.thingsboard.server.service.security.permission.Operation;
import org.thingsboard.server.service.telemetry.AttributeData;
import org.thingsboard.server.service.telemetry.TsData;
import org.thingsboard.server.service.telemetry.exception.InvalidParametersException;
import org.thingsboard.server.service.telemetry.exception.UncheckedApiException;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@TbCoreComponent
@Slf4j
public abstract class AbstractTelemetryController extends AbstractSendRpcController {

  @Autowired
  private TimeseriesService tsService;

  @Autowired
  private AccessValidator accessValidator;

  @Value("${transport.json.max_string_value_length:0}")
  private int maxStringValueLength;

  private ExecutorService executor;

  @PostConstruct
  public void initExecutor() {
    executor = Executors.newSingleThreadExecutor(ThingsBoardThreadFactory.forName("telemetry-controller"));
  }

  @PreDestroy
  public void shutdownExecutor() {
    if (executor != null) {
      executor.shutdownNow();
    }
  }


  private DeferredResult<ResponseEntity> deleteAttributes(EntityId entityIdSrc, String scope, String keysStr) throws ThingsboardException {
    List<String> keys = toKeysList(keysStr);
    if (keys.isEmpty()) {
      return getImmediateDeferredResult("Empty keys: " + keysStr, HttpStatus.BAD_REQUEST);
    }
    SecurityUser user = getCurrentUser();

    if (DataConstants.SERVER_SCOPE.equals(scope) ||
        DataConstants.SHARED_SCOPE.equals(scope) ||
        DataConstants.CLIENT_SCOPE.equals(scope)) {
      return accessValidator.validateEntityAndCallback(getCurrentUser(), Operation.WRITE_ATTRIBUTES, entityIdSrc, (result, tenantId, entityId) -> {
        tsSubService.deleteAndNotify(tenantId, entityId, scope, keys, new FutureCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void tmp) {
//            logAttributesDeleted(user, entityId, scope, keys, null);
            if (entityIdSrc.getEntityType().equals(EntityType.DEVICE)) {
              DeviceId deviceId = new DeviceId(entityId.getId());
              Set<AttributeKey> keysToNotify = new HashSet<>();
              keys.forEach(key -> keysToNotify.add(new AttributeKey(scope, key)));
              tbClusterService.pushMsgToCore(DeviceAttributesEventNotificationMsg.onDelete(
                  user.getTenantId(), deviceId, keysToNotify), null);
            }
            result.setResult(new ResponseEntity<>(HttpStatus.OK));
          }

          @Override
          public void onFailure(Throwable t) {
//            logAttributesDeleted(user, entityId, scope, keys, t);
            result.setResult(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
          }
        });
      });
    } else {
      return getImmediateDeferredResult("Invalid attribute scope: " + scope, HttpStatus.BAD_REQUEST);
    }
  }

  protected DeferredResult<ResponseEntity> saveAttributes(TenantId srcTenantId, EntityId entityIdSrc, String scope, JsonNode json, Object resposeObject) throws ThingsboardException {
    if (!DataConstants.SERVER_SCOPE.equals(scope) && !DataConstants.SHARED_SCOPE.equals(scope)) {
      return getImmediateDeferredResult("Invalid scope: " + scope, HttpStatus.BAD_REQUEST);
    }
    if (json.isObject()) {
      List<AttributeKvEntry> attributes = extractRequestAttributes(json);
      attributes.forEach(ConstraintValidator::validateFields);
      if (attributes.isEmpty()) {
        return getImmediateDeferredResult("No attributes data found in request body!", HttpStatus.BAD_REQUEST);
      }
      for (AttributeKvEntry attributeKvEntry : attributes) {
        if (attributeKvEntry.getKey().isEmpty() || attributeKvEntry.getKey().trim().length() == 0) {
          return getImmediateDeferredResult("Key cannot be empty or contains only spaces", HttpStatus.BAD_REQUEST);
        }
      }
      return accessValidator.validateEntityAndCallback(getCurrentUser(), Operation.WRITE_ATTRIBUTES, entityIdSrc, (result, tenantId, entityId) -> {
        tsSubService.saveAndNotify(tenantId, entityId, scope, attributes, new FutureCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void tmp) {
//            logAttributesUpdated(user, entityId, scope, attributes, null);
            result.setResult(new ResponseEntity(resposeObject, HttpStatus.OK));
          }

          @Override
          public void onFailure(Throwable t) {
//            logAttributesUpdated(user, entityId, scope, attributes, t);
            AccessValidator.handleError(t, result, HttpStatus.INTERNAL_SERVER_ERROR);
          }
        });
      });
    } else {
      return getImmediateDeferredResult("Request is not a JSON object", HttpStatus.BAD_REQUEST);
    }
  }

  private DeferredResult<ResponseEntity> saveTelemetry(TenantId curTenantId, EntityId entityIdSrc, String requestBody, long ttl) throws ThingsboardException {
    Map<Long, List<KvEntry>> telemetryRequest;
    JsonElement telemetryJson;
    try {
      telemetryJson = new JsonParser().parse(requestBody);
    } catch (Exception e) {
      return getImmediateDeferredResult("Unable to parse timeseries payload: Invalid JSON body!", HttpStatus.BAD_REQUEST);
    }
    try {
      telemetryRequest = JsonConverter.convertToTelemetry(telemetryJson, System.currentTimeMillis());
    } catch (Exception e) {
      return getImmediateDeferredResult("Unable to parse timeseries payload. Invalid JSON body: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    List<TsKvEntry> entries = new ArrayList<>();
    for (Map.Entry<Long, List<KvEntry>> entry : telemetryRequest.entrySet()) {
      for (KvEntry kv : entry.getValue()) {
        entries.add(new BasicTsKvEntry(entry.getKey(), kv));
      }
    }
    if (entries.isEmpty()) {
      return getImmediateDeferredResult("No timeseries data found in request body!", HttpStatus.BAD_REQUEST);
    }
    SecurityUser user = getCurrentUser();
    return accessValidator.validateEntityAndCallback(getCurrentUser(), Operation.WRITE_TELEMETRY, entityIdSrc, (result, tenantId, entityId) -> {
      long tenantTtl = ttl;
      if (!TenantId.SYS_TENANT_ID.equals(tenantId) && tenantTtl == 0) {
        TenantProfile tenantProfile = tenantProfileCache.get(tenantId);
        tenantTtl = TimeUnit.DAYS.toSeconds(((DefaultTenantProfileConfiguration) tenantProfile.getProfileData().getConfiguration()).getDefaultStorageTtlDays());
      }
      tsSubService.saveAndNotify(tenantId, user.getCustomerId(), entityId, entries, tenantTtl, new FutureCallback<Void>() {
        @Override
        public void onSuccess(@Nullable Void tmp) {
//          logTelemetryUpdated(user, entityId, entries, null);
          result.setResult(new ResponseEntity(HttpStatus.OK));
        }

        @Override
        public void onFailure(Throwable t) {
//          logTelemetryUpdated(user, entityId, entries, t);
          AccessValidator.handleError(t, result, HttpStatus.INTERNAL_SERVER_ERROR);
        }
      });
    });
  }

  private void getLatestTimeseriesValuesCallback(@Nullable DeferredResult<ResponseEntity> result, SecurityUser user, EntityId entityId, String keys, Boolean useStrictDataTypes) {
    ListenableFuture<List<TsKvEntry>> future;
    if (StringUtils.isEmpty(keys)) {
      future = tsService.findAllLatest(user.getTenantId(), entityId);
    } else {
      future = tsService.findLatest(user.getTenantId(), entityId, toKeysList(keys));
    }
    Futures.addCallback(future, getTsKvListCallback(result, useStrictDataTypes), MoreExecutors.directExecutor());
  }

  private void getAttributeValuesCallback(@Nullable DeferredResult<ResponseEntity> result, SecurityUser user, EntityId entityId, String scope, String keys) {
    List<String> keyList = toKeysList(keys);
    FutureCallback<List<AttributeKvEntry>> callback = getAttributeValuesToResponseCallback(result, user, scope, entityId, keyList);
    if (!StringUtils.isEmpty(scope)) {
      if (keyList != null && !keyList.isEmpty()) {
        Futures.addCallback(attributesService.find(user.getTenantId(), entityId, scope, keyList), callback, MoreExecutors.directExecutor());
      } else {
        Futures.addCallback(attributesService.findAll(user.getTenantId(), entityId, scope), callback, MoreExecutors.directExecutor());
      }
    } else {
      List<ListenableFuture<List<AttributeKvEntry>>> futures = new ArrayList<>();
      for (String tmpScope : DataConstants.allScopes()) {
        if (keyList != null && !keyList.isEmpty()) {
          futures.add(attributesService.find(user.getTenantId(), entityId, tmpScope, keyList));
        } else {
          futures.add(attributesService.findAll(user.getTenantId(), entityId, tmpScope));
        }
      }

      ListenableFuture<List<AttributeKvEntry>> future = mergeAllAttributesFutures(futures);

      Futures.addCallback(future, callback, MoreExecutors.directExecutor());
    }
  }

  private void getAttributeKeysCallback(@Nullable DeferredResult<ResponseEntity> result, TenantId tenantId, EntityId entityId, String scope) {
    Futures.addCallback(attributesService.findAll(tenantId, entityId, scope), getAttributeKeysToResponseCallback(result), MoreExecutors.directExecutor());
  }

  private void getAttributeKeysCallback(@Nullable DeferredResult<ResponseEntity> result, TenantId tenantId, EntityId entityId) {
    List<ListenableFuture<List<AttributeKvEntry>>> futures = new ArrayList<>();
    for (String scope : DataConstants.allScopes()) {
      futures.add(attributesService.findAll(tenantId, entityId, scope));
    }

    ListenableFuture<List<AttributeKvEntry>> future = mergeAllAttributesFutures(futures);

    Futures.addCallback(future, getAttributeKeysToResponseCallback(result), MoreExecutors.directExecutor());
  }

  private FutureCallback<List<TsKvEntry>> getTsKeysToResponseCallback(final DeferredResult<ResponseEntity> response) {
    return new FutureCallback<>() {
      @Override
      public void onSuccess(List<TsKvEntry> values) {
        List<String> keys = values.stream().map(KvEntry::getKey).collect(Collectors.toList());
        response.setResult(new ResponseEntity<>(keys, HttpStatus.OK));
      }

      @Override
      public void onFailure(Throwable e) {
        log.error("Failed to fetch attributes", e);
        AccessValidator.handleError(e, response, HttpStatus.INTERNAL_SERVER_ERROR);
      }
    };
  }

  private FutureCallback<List<AttributeKvEntry>> getAttributeKeysToResponseCallback(final DeferredResult<ResponseEntity> response) {
    return new FutureCallback<List<AttributeKvEntry>>() {

      @Override
      public void onSuccess(List<AttributeKvEntry> attributes) {
        List<String> keys = attributes.stream().map(KvEntry::getKey).collect(Collectors.toList());
        response.setResult(new ResponseEntity<>(keys, HttpStatus.OK));
      }

      @Override
      public void onFailure(Throwable e) {
        log.error("Failed to fetch attributes", e);
        AccessValidator.handleError(e, response, HttpStatus.INTERNAL_SERVER_ERROR);
      }
    };
  }

  private FutureCallback<List<AttributeKvEntry>> getAttributeValuesToResponseCallback(final DeferredResult<ResponseEntity> response,
                                                                                      final SecurityUser user, final String scope,
                                                                                      final EntityId entityId, final List<String> keyList) {
    return new FutureCallback<>() {
      @Override
      public void onSuccess(List<AttributeKvEntry> attributes) {
        List<AttributeData> values = attributes.stream().map(attribute ->
            new AttributeData(attribute.getLastUpdateTs(), attribute.getKey(), getKvValue(attribute))
        ).collect(Collectors.toList());
//        logAttributesRead(user, entityId, scope, keyList, null);
        response.setResult(new ResponseEntity<>(values, HttpStatus.OK));
      }

      @Override
      public void onFailure(Throwable e) {
        log.error("Failed to fetch attributes", e);
//        logAttributesRead(user, entityId, scope, keyList, e);
        AccessValidator.handleError(e, response, HttpStatus.INTERNAL_SERVER_ERROR);
      }
    };
  }

  private FutureCallback<List<TsKvEntry>> getTsKvListCallback(final DeferredResult<ResponseEntity> response, Boolean useStrictDataTypes) {
    return new FutureCallback<>() {
      @Override
      public void onSuccess(List<TsKvEntry> data) {
        Map<String, List<TsData>> result = new LinkedHashMap<>();
        for (TsKvEntry entry : data) {
          Object value = useStrictDataTypes ? getKvValue(entry) : entry.getValueAsString();
          result.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(new TsData(entry.getTs(), value));
        }
        response.setResult(new ResponseEntity<>(result, HttpStatus.OK));
      }

      @Override
      public void onFailure(Throwable e) {
        log.error("Failed to fetch historical data", e);
        AccessValidator.handleError(e, response, HttpStatus.INTERNAL_SERVER_ERROR);
      }
    };
  }

  private ListenableFuture<List<AttributeKvEntry>> mergeAllAttributesFutures(List<ListenableFuture<List<AttributeKvEntry>>> futures) {
    return Futures.transform(Futures.successfulAsList(futures),
        (Function<? super List<List<AttributeKvEntry>>, ? extends List<AttributeKvEntry>>) input -> {
          List<AttributeKvEntry> tmp = new ArrayList<>();
          if (input != null) {
            input.forEach(tmp::addAll);
          }
          return tmp;
        }, executor);
  }

  private List<String> toKeysList(String keys) {
    List<String> keyList = null;
    if (!StringUtils.isEmpty(keys)) {
      keyList = Arrays.asList(keys.split(","));
    }
    return keyList;
  }

  private DeferredResult<ResponseEntity> getImmediateDeferredResult(String message, HttpStatus status) {
    DeferredResult<ResponseEntity> result = new DeferredResult<>();
    result.setResult(new ResponseEntity<>(message, status));
    return result;
  }

  private List<AttributeKvEntry> extractRequestAttributes(JsonNode jsonNode) {
    long ts = System.currentTimeMillis();
    List<AttributeKvEntry> attributes = new ArrayList<>();
    jsonNode.fields().forEachRemaining(entry -> {
      String key = entry.getKey();
      JsonNode value = entry.getValue();
      if (entry.getValue().isObject() || entry.getValue().isArray()) {
        attributes.add(new BaseAttributeKvEntry(new JsonDataEntry(key, toJsonStr(value)), ts));
      } else if (entry.getValue().isTextual()) {
        if (maxStringValueLength > 0 && entry.getValue().textValue().length() > maxStringValueLength) {
          String message = String.format("String value length [%d] for key [%s] is greater than maximum allowed [%d]", entry.getValue().textValue().length(), key, maxStringValueLength);
          throw new UncheckedApiException(new InvalidParametersException(message));
        }
        attributes.add(new BaseAttributeKvEntry(new StringDataEntry(key, value.textValue()), ts));
      } else if (entry.getValue().isBoolean()) {
        attributes.add(new BaseAttributeKvEntry(new BooleanDataEntry(key, value.booleanValue()), ts));
      } else if (entry.getValue().isDouble()) {
        attributes.add(new BaseAttributeKvEntry(new DoubleDataEntry(key, value.doubleValue()), ts));
      } else if (entry.getValue().isNumber()) {
        if (entry.getValue().isBigInteger()) {
          throw new UncheckedApiException(new InvalidParametersException("Big integer values are not supported!"));
        } else {
          attributes.add(new BaseAttributeKvEntry(new LongDataEntry(key, value.longValue()), ts));
        }
      }
    });
    return attributes;
  }

  private String toJsonStr(JsonNode value) {
    try {
      return JacksonUtil.toString(value);
    } catch (IllegalArgumentException e) {
      throw new JsonParseException("Can't parse jsonValue: " + value, e);
    }
  }

  private JsonNode toJsonNode(String value) {
    try {
      return JacksonUtil.toJsonNode(value);
    } catch (IllegalArgumentException e) {
      throw new JsonParseException("Can't parse jsonValue: " + value, e);
    }
  }

  private Object getKvValue(KvEntry entry) {
    if (entry.getDataType() == DataType.JSON) {
      return toJsonNode(entry.getJsonValue().get());
    }
    return entry.getValue();
  }
}
