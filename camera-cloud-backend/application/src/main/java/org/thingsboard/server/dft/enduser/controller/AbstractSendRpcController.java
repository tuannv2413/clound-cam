package org.thingsboard.server.dft.enduser.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.FutureCallback;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.async.DeferredResult;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.rule.engine.api.msg.DeviceNameOrTypeUpdateMsg;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.device.profile.DeviceProfileData;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.BaseAttributeKvEntry;
import org.thingsboard.server.common.data.kv.JsonDataEntry;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;
import org.thingsboard.server.common.data.rpc.RpcError;
import org.thingsboard.server.common.data.rpc.ToDeviceRpcRequestBody;
import org.thingsboard.server.common.msg.rpc.FromDeviceRpcResponse;
import org.thingsboard.server.common.msg.rpc.ToDeviceRpcRequest;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.controller.HttpValidationCallback;
import org.thingsboard.server.dao.settings.AdminSettingsService;
import org.thingsboard.server.dft.enduser.dto.box.BoxDetailDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraEditDto;
import org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.CameraSettingAttribute;
import org.thingsboard.server.dft.mbgadmin.dto.setting.AntMediaServerDto;
import org.thingsboard.server.dft.util.service.AntMediaClient;
import org.thingsboard.server.exception.ThingsboardErrorResponse;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.rpc.LocalRequestMetaData;
import org.thingsboard.server.service.rpc.TbCoreDeviceRpcService;
import org.thingsboard.server.service.security.AccessValidator;
import org.thingsboard.server.service.security.model.SecurityUser;
import org.thingsboard.server.service.security.permission.Operation;
import org.thingsboard.server.service.telemetry.exception.ToErrorResponseEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@TbCoreComponent
@Slf4j
public abstract class AbstractSendRpcController extends BaseController {

  protected static final String AUTO = "AUTO";
  protected static final String MANUAL = "MANUAL";

  protected final ObjectMapper mapper = new ObjectMapper();

  @Autowired
  protected TbCoreDeviceRpcService deviceRpcService;

  @Autowired
  protected AccessValidator accessValidator;

  @Value("${server.rest.server_side_rpc.min_timeout:5000}")
  protected long minTimeout;

  @Value("${server.rest.server_side_rpc.default_timeout:10000}")
  protected long defaultTimeout;

  protected static final String MAIN_RTSP = "main_rtsp";
  protected static final String SUB_RTSP = "sub_rtsp";

  @Autowired
  AntMediaClient antMediaClient;

  @Autowired
  AdminSettingsService adminSettingsService;

  protected void handleDeviceRPCRequestNoResponse(
      DeviceId deviceId, String requestBody, SecurityUser currentUser) throws ThingsboardException {
    try {
      JsonNode rpcRequestBody = JacksonUtil.toJsonNode(requestBody);
      ToDeviceRpcRequestBody body =
          new ToDeviceRpcRequestBody(
              rpcRequestBody.get("method").asText(),
              JacksonUtil.toString(rpcRequestBody.get("params")));
      TenantId tenantId = currentUser.getTenantId();
      long timeout =
          rpcRequestBody.has(DataConstants.TIMEOUT)
              ? rpcRequestBody.get(DataConstants.TIMEOUT).asLong()
              : defaultTimeout;
      long expTime =
          rpcRequestBody.has(DataConstants.EXPIRATION_TIME)
              ? rpcRequestBody.get(DataConstants.EXPIRATION_TIME).asLong()
              : System.currentTimeMillis() + Math.max(minTimeout, timeout);
      UUID rpcRequestUUID =
          rpcRequestBody.has("requestUUID")
              ? UUID.fromString(rpcRequestBody.get("requestUUID").asText())
              : UUID.randomUUID();
      boolean persisted =
          rpcRequestBody.has(DataConstants.PERSISTENT)
              && rpcRequestBody.get(DataConstants.PERSISTENT).asBoolean();
      String additionalInfo =
          JacksonUtil.toString(rpcRequestBody.get(DataConstants.ADDITIONAL_INFO));
      Integer retries =
          rpcRequestBody.has(DataConstants.RETRIES)
              ? rpcRequestBody.get(DataConstants.RETRIES).asInt()
              : null;
      ToDeviceRpcRequest rpcRequest =
          new ToDeviceRpcRequest(
              rpcRequestUUID,
              tenantId,
              deviceId,
              true,
              expTime,
              body,
              persisted,
              retries,
              additionalInfo);
      deviceRpcService.processRestApiRpcRequest(rpcRequest, this::replyNoResponse, currentUser);
    } catch (IllegalArgumentException ioe) {
      throw new ThingsboardException(
          "Invalid request body", ioe, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
    }
  }

  private void replyNoResponse(FromDeviceRpcResponse response) {
    Optional<RpcError> rpcError = response.getError();
    if (rpcError.isPresent()) {
      RpcError error = rpcError.get();
      switch (error) {
        case TIMEOUT:
          log.info("Request Time out");
          break;
        case NO_ACTIVE_CONNECTION:
          log.info("Request NO_ACTIVE_CONNECTION");
          break;
        default:
          break;
      }
    } else {
      Optional<String> responseData = response.getResponse();
      if (responseData.isPresent() && !StringUtils.isEmpty(responseData.get())) {
        String data = responseData.get();
        try {
          log.info("Request Sucess");
        } catch (IllegalArgumentException e) {
          log.debug("Failed to decode device response: {}", data, e);
        }
      } else {
        log.info("Request Sucess");
      }
    }
  }

  protected DeferredResult<ResponseEntity> handleDeviceRPCRequest(
      boolean oneWay,
      DeviceId deviceId,
      String requestBody,
      HttpStatus timeoutStatus,
      HttpStatus noActiveConnectionStatus)
      throws ThingsboardException {
    try {
      JsonNode rpcRequestBody = JacksonUtil.toJsonNode(requestBody);
      ToDeviceRpcRequestBody body =
          new ToDeviceRpcRequestBody(
              rpcRequestBody.get("method").asText(),
              JacksonUtil.toString(rpcRequestBody.get("params")));
      SecurityUser currentUser = getCurrentUser();
      TenantId tenantId = currentUser.getTenantId();
      final DeferredResult<ResponseEntity> response = new DeferredResult<>();
      long timeout =
          rpcRequestBody.has(DataConstants.TIMEOUT)
              ? rpcRequestBody.get(DataConstants.TIMEOUT).asLong()
              : defaultTimeout;
      long expTime =
          rpcRequestBody.has(DataConstants.EXPIRATION_TIME)
              ? rpcRequestBody.get(DataConstants.EXPIRATION_TIME).asLong()
              : System.currentTimeMillis() + Math.max(minTimeout, timeout);
      UUID rpcRequestUUID =
          rpcRequestBody.has("requestUUID")
              ? UUID.fromString(rpcRequestBody.get("requestUUID").asText())
              : UUID.randomUUID();
      boolean persisted =
          rpcRequestBody.has(DataConstants.PERSISTENT)
              && rpcRequestBody.get(DataConstants.PERSISTENT).asBoolean();
      String additionalInfo =
          JacksonUtil.toString(rpcRequestBody.get(DataConstants.ADDITIONAL_INFO));
      Integer retries =
          rpcRequestBody.has(DataConstants.RETRIES)
              ? rpcRequestBody.get(DataConstants.RETRIES).asInt()
              : null;
      accessValidator.validate(
          currentUser,
          Operation.RPC_CALL,
          deviceId,
          new HttpValidationCallback(
              response,
              new FutureCallback<>() {
                @Override
                public void onSuccess(@Nullable DeferredResult<ResponseEntity> result) {
                  ToDeviceRpcRequest rpcRequest =
                      new ToDeviceRpcRequest(
                          rpcRequestUUID,
                          tenantId,
                          deviceId,
                          oneWay,
                          expTime,
                          body,
                          persisted,
                          retries,
                          additionalInfo);
                  deviceRpcService.processRestApiRpcRequest(
                      rpcRequest,
                      fromDeviceRpcResponse ->
                          reply(
                              new LocalRequestMetaData(rpcRequest, currentUser, result),
                              fromDeviceRpcResponse,
                              timeoutStatus,
                              noActiveConnectionStatus),
                      currentUser);
                }

                @Override
                public void onFailure(Throwable e) {
                  ResponseEntity entity;
                  if (e instanceof ToErrorResponseEntity) {
                    entity = ((ToErrorResponseEntity) e).toErrorResponseEntity();
                  } else {
                    entity = new ResponseEntity(HttpStatus.UNAUTHORIZED);
                  }
                  //          logRpcCall(currentUser, deviceId, body, oneWay, Optional.empty(), e);
                  response.setResult(entity);
                }
              }));
      return response;
    } catch (IllegalArgumentException ioe) {
      throw new ThingsboardException(
          "Invalid request body", ioe, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
    }
  }

  public void reply(
      LocalRequestMetaData rpcRequest,
      FromDeviceRpcResponse response,
      HttpStatus timeoutStatus,
      HttpStatus noActiveConnectionStatus) {
    Optional<RpcError> rpcError = response.getError();
    DeferredResult<ResponseEntity> responseWriter = rpcRequest.getResponseWriter();
    if (rpcError.isPresent()) {
      //      logRpcCall(rpcRequest, rpcError, null);
      RpcError error = rpcError.get();
      switch (error) {
        case TIMEOUT:
          responseWriter.setResult(new ResponseEntity<>(timeoutStatus));
          break;
        case NO_ACTIVE_CONNECTION:
          responseWriter.setResult(new ResponseEntity<>(noActiveConnectionStatus));
          break;
        default:
          responseWriter.setResult(new ResponseEntity<>(timeoutStatus));
          break;
      }
    } else {
      Optional<String> responseData = response.getResponse();
      if (responseData.isPresent() && !StringUtils.isEmpty(responseData.get())) {
        String data = responseData.get();
        try {
          //          logRpcCall(rpcRequest, rpcError, null);
          responseWriter.setResult(
              new ResponseEntity<>(JacksonUtil.toJsonNode(data), HttpStatus.OK));
        } catch (IllegalArgumentException e) {
          log.debug("Failed to decode device response: {}", data, e);
          //          logRpcCall(rpcRequest, rpcError, e);
          responseWriter.setResult(new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE));
        }
      } else {
        //        logRpcCall(rpcRequest, rpcError, null);
        responseWriter.setResult(new ResponseEntity<>(HttpStatus.OK));
      }
    }
  }

  protected DeferredResult<ResponseEntity> handleAddDeviceRPCRequest(
      boolean oneWay,
      DeviceId deviceId,
      String requestBody,
      HttpStatus timeoutStatus,
      HttpStatus noActiveConnectionStatus,
      CameraEditDto cameraEditDto,
      SecurityUser securityUser)
      throws ThingsboardException {
    try {
      JsonNode rpcRequestBody = JacksonUtil.toJsonNode(requestBody);
      ToDeviceRpcRequestBody body =
          new ToDeviceRpcRequestBody(
              rpcRequestBody.get("method").asText(),
              JacksonUtil.toString(rpcRequestBody.get("params")));
      SecurityUser currentUser = getCurrentUser();
      TenantId tenantId = currentUser.getTenantId();
      final DeferredResult<ResponseEntity> response = new DeferredResult<>();
      long timeout =
          rpcRequestBody.has(DataConstants.TIMEOUT)
              ? rpcRequestBody.get(DataConstants.TIMEOUT).asLong()
              : defaultTimeout;
      long expTime =
          rpcRequestBody.has(DataConstants.EXPIRATION_TIME)
              ? rpcRequestBody.get(DataConstants.EXPIRATION_TIME).asLong()
              : System.currentTimeMillis() + Math.max(minTimeout, timeout);
      UUID rpcRequestUUID =
          rpcRequestBody.has("requestUUID")
              ? UUID.fromString(rpcRequestBody.get("requestUUID").asText())
              : UUID.randomUUID();
      boolean persisted =
          rpcRequestBody.has(DataConstants.PERSISTENT)
              && rpcRequestBody.get(DataConstants.PERSISTENT).asBoolean();
      String additionalInfo =
          JacksonUtil.toString(rpcRequestBody.get(DataConstants.ADDITIONAL_INFO));
      Integer retries =
          rpcRequestBody.has(DataConstants.RETRIES)
              ? rpcRequestBody.get(DataConstants.RETRIES).asInt()
              : null;
      accessValidator.validate(
          currentUser,
          Operation.RPC_CALL,
          deviceId,
          new HttpValidationCallback(
              response,
              new FutureCallback<>() {
                @Override
                public void onSuccess(@Nullable DeferredResult<ResponseEntity> result) {
                  ToDeviceRpcRequest rpcRequest =
                      new ToDeviceRpcRequest(
                          rpcRequestUUID,
                          tenantId,
                          deviceId,
                          oneWay,
                          expTime,
                          body,
                          persisted,
                          retries,
                          additionalInfo);
                  deviceRpcService.processRestApiRpcRequest(
                      rpcRequest,
                      fromDeviceRpcResponse -> {
                        try {
                          replyAddDevice(
                              new LocalRequestMetaData(rpcRequest, currentUser, result),
                              fromDeviceRpcResponse,
                              timeoutStatus,
                              noActiveConnectionStatus,
                              cameraEditDto,
                              securityUser);
                        } catch (JsonProcessingException | ThingsboardException e) {
                          e.printStackTrace();
                        }
                      },
                      currentUser);
                }

                @Override
                public void onFailure(Throwable e) {
                  ResponseEntity entity;
                  if (e instanceof ToErrorResponseEntity) {
                    entity = ((ToErrorResponseEntity) e).toErrorResponseEntity();
                  } else {
                    entity = new ResponseEntity(HttpStatus.UNAUTHORIZED);
                  }
                  //          logRpcCall(currentUser, deviceId, body, oneWay, Optional.empty(), e);
                  response.setResult(entity);
                }
              }));
      return response;
    } catch (IllegalArgumentException ioe) {
      throw new ThingsboardException(
          "Invalid request body", ioe, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
    }
  }

  public void replyAddDevice(
      LocalRequestMetaData rpcRequest,
      FromDeviceRpcResponse response,
      HttpStatus timeoutStatus,
      HttpStatus noActiveConnectionStatus,
      CameraEditDto cameraEditDto,
      SecurityUser securityUser)
      throws JsonProcessingException, ThingsboardException {
    Optional<RpcError> rpcError = response.getError();
    DeferredResult<ResponseEntity> responseWriter = rpcRequest.getResponseWriter();
    if (rpcError.isPresent()) {
      //      logRpcCall(rpcRequest, rpcError, null);
      RpcError error = rpcError.get();
      switch (error) {
        case TIMEOUT:
          responseWriter.setResult(new ResponseEntity<>(timeoutStatus));
          break;
        case NO_ACTIVE_CONNECTION:
          responseWriter.setResult(new ResponseEntity<>(noActiveConnectionStatus));
          break;
        default:
          responseWriter.setResult(new ResponseEntity<>(timeoutStatus));
          break;
      }
      antMediaClient.deleteStream(cameraEditDto.getRtmpStreamId());
    } else {
      Optional<String> responseData = response.getResponse();
      if (responseData.isPresent() && !StringUtils.isEmpty(responseData.get())) {
        String data = responseData.get();
        try {
          //          logRpcCall(rpcRequest, rpcError, null);
          JsonNode gatewayResponse = JacksonUtil.toJsonNode(data);
          if (gatewayResponse.get("added").asBoolean()) {
            if (gatewayResponse.get("isOnvif").asBoolean()) {
              if (gatewayResponse.get("rtspUrl") != null
                  && !gatewayResponse.get("rtspUrl").asText().equals("")) {
                cameraEditDto.setMainRtspUrl(gatewayResponse.get("rtspUrl").asText());
                cameraEditDto.setSubRtspUrl(gatewayResponse.get("rtspUrl").asText());

                if (gatewayResponse.get("channel") != null
                    && !gatewayResponse.get("channel").asText().equals("")) {
                  cameraEditDto.setChannel(gatewayResponse.get("channel").asText());
                } else {
                  cameraEditDto.setChannel("mainStream");
                }
              }
            }
            cameraEditDto = save(cameraEditDto, securityUser);
            cameraLiveService.saveToStreamViewer(cameraEditDto.getId(), securityUser);
          } else {
            if (gatewayResponse.get("errorCode").asInt() == 1) {
              responseWriter.setResult(new ResponseEntity<>(ThingsboardErrorResponse.of("Tài khoản hoặc mật khẩu onvif không chính xác!",
                  ThingsboardErrorCode.GENERAL, HttpStatus.EXPECTATION_FAILED), HttpStatus.INTERNAL_SERVER_ERROR));
            } else if (gatewayResponse.get("errorCode").asInt() == 2) {
              responseWriter.setResult(new ResponseEntity<>(ThingsboardErrorResponse.of("Không thể kết stream bằng đường dẫn rtsp!",
                  ThingsboardErrorCode.GENERAL, HttpStatus.EXPECTATION_FAILED), HttpStatus.INTERNAL_SERVER_ERROR));
            } else {
              responseWriter.setResult(new ResponseEntity<>(ThingsboardErrorResponse.of("Box gặp lỗi không xác định!",
                  ThingsboardErrorCode.GENERAL, HttpStatus.EXPECTATION_FAILED), HttpStatus.INTERNAL_SERVER_ERROR));
            }
          }
          responseWriter.setResult(new ResponseEntity<>(cameraEditDto, HttpStatus.OK));
        } catch (IllegalArgumentException e) {
          log.debug("Failed to decode device response: {}", data, e);
          //          logRpcCall(rpcRequest, rpcError, e);
          antMediaClient.deleteStream(cameraEditDto.getRtmpStreamId());
          responseWriter.setResult(new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE));
        } catch (ThingsboardException
            | JSONException
            | ExecutionException
            | InterruptedException e) {
          antMediaClient.deleteStream(cameraEditDto.getRtmpStreamId());
          responseWriter.setResult(new ResponseEntity(ThingsboardErrorResponse.of(e.getMessage(),
              ThingsboardErrorCode.GENERAL, HttpStatus.EXPECTATION_FAILED),
              HttpStatus.EXPECTATION_FAILED));
        }
      } else {
        //        logRpcCall(rpcRequest, rpcError, null);
        responseWriter.setResult(new ResponseEntity<>(HttpStatus.OK));
      }
    }
  }

  // Hanlde request changebox
  protected DeferredResult<ResponseEntity> handleChangeBoxRPCRequest(
      boolean oneWay,
      DeviceId deviceId,
      String requestBody,
      HttpStatus timeoutStatus,
      HttpStatus noActiveConnectionStatus,
      CameraEditDto cameraEditDto,
      UUID newBoxId,
      SecurityUser securityUser)
      throws ThingsboardException {
    try {
      JsonNode rpcRequestBody = JacksonUtil.toJsonNode(requestBody);
      ToDeviceRpcRequestBody body =
          new ToDeviceRpcRequestBody(
              rpcRequestBody.get("method").asText(),
              JacksonUtil.toString(rpcRequestBody.get("params")));
      SecurityUser currentUser = getCurrentUser();
      TenantId tenantId = currentUser.getTenantId();
      final DeferredResult<ResponseEntity> response = new DeferredResult<>();
      long timeout =
          rpcRequestBody.has(DataConstants.TIMEOUT)
              ? rpcRequestBody.get(DataConstants.TIMEOUT).asLong()
              : defaultTimeout;
      long expTime =
          rpcRequestBody.has(DataConstants.EXPIRATION_TIME)
              ? rpcRequestBody.get(DataConstants.EXPIRATION_TIME).asLong()
              : System.currentTimeMillis() + Math.max(minTimeout, timeout);
      UUID rpcRequestUUID =
          rpcRequestBody.has("requestUUID")
              ? UUID.fromString(rpcRequestBody.get("requestUUID").asText())
              : UUID.randomUUID();
      boolean persisted =
          rpcRequestBody.has(DataConstants.PERSISTENT)
              && rpcRequestBody.get(DataConstants.PERSISTENT).asBoolean();
      String additionalInfo =
          JacksonUtil.toString(rpcRequestBody.get(DataConstants.ADDITIONAL_INFO));
      Integer retries =
          rpcRequestBody.has(DataConstants.RETRIES)
              ? rpcRequestBody.get(DataConstants.RETRIES).asInt()
              : null;
      accessValidator.validate(
          currentUser,
          Operation.RPC_CALL,
          deviceId,
          new HttpValidationCallback(
              response,
              new FutureCallback<>() {
                @Override
                public void onSuccess(@Nullable DeferredResult<ResponseEntity> result) {
                  ToDeviceRpcRequest rpcRequest =
                      new ToDeviceRpcRequest(
                          rpcRequestUUID,
                          tenantId,
                          deviceId,
                          oneWay,
                          expTime,
                          body,
                          persisted,
                          retries,
                          additionalInfo);
                  deviceRpcService.processRestApiRpcRequest(
                      rpcRequest,
                      fromDeviceRpcResponse -> {
                        try {
                          replyChangeDevice(
                              new LocalRequestMetaData(rpcRequest, currentUser, result),
                              fromDeviceRpcResponse,
                              timeoutStatus,
                              noActiveConnectionStatus,
                              cameraEditDto,
                              newBoxId,
                              securityUser);
                        } catch (JsonProcessingException | ThingsboardException e) {
                          handleException(e);
                        }
                      },
                      currentUser);
                }

                @Override
                public void onFailure(Throwable e) {
                  ResponseEntity entity;
                  if (e instanceof ToErrorResponseEntity) {
                    entity = ((ToErrorResponseEntity) e).toErrorResponseEntity();
                  } else {
                    entity = new ResponseEntity(HttpStatus.UNAUTHORIZED);
                  }
                  //          logRpcCall(currentUser, deviceId, body, oneWay, Optional.empty(), e);
                  response.setResult(entity);
                }
              }));
      return response;
    } catch (IllegalArgumentException ioe) {
      throw new ThingsboardException(
          "Invalid request body", ioe, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
    }
  }

  public void replyChangeDevice(
      LocalRequestMetaData rpcRequest,
      FromDeviceRpcResponse response,
      HttpStatus timeoutStatus,
      HttpStatus noActiveConnectionStatus,
      CameraEditDto cameraEditDto,
      UUID newBoxId,
      SecurityUser securityUser)
      throws JsonProcessingException, ThingsboardException {
    Optional<RpcError> rpcError = response.getError();
    DeferredResult<ResponseEntity> responseWriter = rpcRequest.getResponseWriter();
    if (rpcError.isPresent()) {
      //      logRpcCall(rpcRequest, rpcError, null);
      RpcError error = rpcError.get();
      switch (error) {
        case NO_ACTIVE_CONNECTION:
          responseWriter.setResult(new ResponseEntity<>(noActiveConnectionStatus));
          break;
        default:
          responseWriter.setResult(new ResponseEntity<>(timeoutStatus));
          break;
      }
    } else {
      Optional<String> responseData = response.getResponse();
      if (responseData.isPresent() && !StringUtils.isEmpty(responseData.get())) {
        String data = responseData.get();
        try {
          //          logRpcCall(rpcRequest, rpcError, null);
          JsonNode gatewayResponse = JacksonUtil.toJsonNode(data);
          if (gatewayResponse.get("added").asBoolean()) {
            UUID oldDeviceId = UUID.fromString(cameraEditDto.getTbDeviceId().toString());
            UUID oldBoxId = UUID.fromString(cameraEditDto.getBoxId().toString());
            cameraEditDto = changeBox(cameraEditDto.getId(), newBoxId, securityUser);
            cameraLiveService.saveToStreamViewer(cameraEditDto.getId(), securityUser);
            // Remove tên thiết bị vào list deviceName
            boxService.removeDeviceNameFromListDeviceName(oldBoxId, oldDeviceId, securityUser);

            // delete old device
            DeviceId oldDeviceCameraId = new DeviceId(oldDeviceId);
            deviceService.deleteDevice(securityUser.getTenantId(), oldDeviceCameraId);
          } else {
            if (gatewayResponse.get("errorCode").asInt() == 1) {
              responseWriter.setResult(new ResponseEntity<>("Tài khoản hoặc mật khẩu onvif không chính xác!", HttpStatus.INTERNAL_SERVER_ERROR));
            } else if (gatewayResponse.get("errorCode").asInt() == 2) {
              responseWriter.setResult(new ResponseEntity<>("Không thể kết stream bằng đường dẫn rtsp!", HttpStatus.INTERNAL_SERVER_ERROR));
            } else {
              responseWriter.setResult(new ResponseEntity<>("Box gặp lỗi không xác định!", HttpStatus.INTERNAL_SERVER_ERROR));
            }
          }
          responseWriter.setResult(new ResponseEntity<>(cameraEditDto, HttpStatus.OK));
        } catch (IllegalArgumentException e) {
          log.debug("Failed to decode device response: {}", data, e);
          responseWriter.setResult(new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE));
        } catch (Exception e) {
          responseWriter.setResult(new ResponseEntity(ThingsboardErrorResponse.of(e.getMessage(),
              ThingsboardErrorCode.GENERAL, HttpStatus.EXPECTATION_FAILED),
              HttpStatus.EXPECTATION_FAILED));
        }
      } else {
        responseWriter.setResult(new ResponseEntity<>(HttpStatus.OK));
      }
    }
  }

  private CameraEditDto save(CameraEditDto cameraEditDto, SecurityUser securityUser)
      throws JSONException, ExecutionException, InterruptedException, ThingsboardException,
      JsonProcessingException {
    // validate dữ liệu
    cameraEditDto.setCameraName(cameraEditDto.getCameraName().trim());

    checkNotNull(cameraEditDto.getId());
    BoxDetailDto boxDetailDto = boxService.getBoxDetailById(cameraEditDto.getBoxId(), securityUser);
    checkNotNull(boxDetailDto);

    RelationTypeGroup typeGroup = RelationTypeGroup.COMMON;
    List<EntityRelation> listDeviceCamId =
        relationService.findByFrom(
            securityUser.getTenantId(), new DeviceId(boxDetailDto.getTbDeviceId()), typeGroup);

    for (EntityRelation entityRelation : listDeviceCamId) {
      if (cameraService.checkCameraIdInDevice(
          securityUser.getTenantId(),
          new DeviceId(entityRelation.getTo().getId()),
          cameraEditDto.getId())) {
        cameraEditDto.setTbDeviceId(entityRelation.getTo().getId());
      }
    }

    checkNotNull(cameraEditDto.getTbDeviceId());

    if (cameraService.existsByTbDeviceIdAndTenantIdAndBoxId(
        cameraEditDto.getTbDeviceId(), securityUser.getTenantId().getId(), cameraEditDto.getBoxId())) {
      throw new ThingsboardException(
          "Thiết bị đã được xác thực với camera khác", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
    }

    // Create new device profile
    createNewDeviceProfile(cameraEditDto, securityUser);
    CameraEditDto cameraEditDtoSaved = cameraService.save(cameraEditDto, securityUser);

    // Lưu setting vào share attribute
    CameraSettingAttribute cameraSettingAttribute = new CameraSettingAttribute(cameraEditDto);
    Gson gson = new Gson();
    String param = gson.toJson(cameraSettingAttribute);
    JSONObject streamOption = new JSONObject(param);
    BaseAttributeKvEntry baseAttributeKvEntry =
        new BaseAttributeKvEntry(
            System.currentTimeMillis(), new JsonDataEntry("streamOption", streamOption.toString()));
    attributesService.save(
        securityUser.getTenantId(),
        new DeviceId(cameraEditDto.getTbDeviceId()),
        DataConstants.SHARED_SCOPE,
        List.of(baseAttributeKvEntry));

    // Add tên thiết bị vào list deviceName
    boxService.saveDeviceNameToListDeviceName(
        cameraEditDtoSaved.getBoxId(), cameraEditDtoSaved.getTbDeviceId(), securityUser);

    checkNotNull(cameraEditDto);
    return cameraEditDto;
  }

  private CameraEditDto changeBox(UUID cameraId, UUID newBoxId, SecurityUser securityUser)
      throws ThingsboardException, ExecutionException, InterruptedException, JSONException,
      JsonProcessingException {
    CameraEditDto cameraEditDto = cameraService.getCameraEditDtoById(cameraId);
    checkNotNull(cameraEditDto);

    UUID oldBoxId = cameraEditDto.getBoxId();

    BoxDetailDto boxDetailDto = boxService.getBoxDetailById(newBoxId, securityUser);
    checkNotNull(boxDetailDto);

    RelationTypeGroup typeGroup = RelationTypeGroup.COMMON;
    List<EntityRelation> listDeviceCamId =
        relationService.findByFrom(
            securityUser.getTenantId(), new DeviceId(boxDetailDto.getTbDeviceId()), typeGroup);
    for (EntityRelation entityRelation : listDeviceCamId) {
      if (cameraService.checkCameraIdInDevice(
          securityUser.getTenantId(),
          new DeviceId(entityRelation.getTo().getId()),
          cameraEditDto.getId())) {
        cameraEditDto.setTbDeviceId(entityRelation.getTo().getId());
      }
    }
    checkNotNull(cameraEditDto.getTbDeviceId());

    if (cameraService.existsByTbDeviceIdAndTenantIdAndBoxId(
        cameraEditDto.getTbDeviceId(), securityUser.getTenantId().getId(), newBoxId)) {
      throw new ThingsboardException(
          "Thiết bị đã được xác thực với camera khác", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
    }

    // Create new deviceProfile
    createNewDeviceProfile(cameraEditDto, securityUser);

    cameraEditDto.setBoxId(newBoxId);
    cameraEditDto = cameraService.changeBox(cameraEditDto, securityUser);
    //Thông báo xóa camera
    JSONObject valueRpc = new JSONObject();
    valueRpc.put("cameraId", cameraEditDto.getId());
    JSONObject requestParam = new JSONObject();
    requestParam.put("value", valueRpc);

    JSONObject requestBody = new JSONObject();
    requestBody.put("method", "DELETE_CAMERA");
    requestBody.put("params", requestParam);
    requestBody.put("retries", 5);
    log.info(requestBody.toString());
    handleDeviceRPCRequestNoResponse(new DeviceId(oldBoxId), requestBody.toString(), securityUser);

    // Lưu setting vào share attribute
    CameraSettingAttribute cameraSettingAttribute = new CameraSettingAttribute(cameraEditDto);
    Gson gson = new Gson();
    String param = gson.toJson(cameraSettingAttribute);
    JSONObject streamOption = new JSONObject(param);
    BaseAttributeKvEntry baseAttributeKvEntry =
        new BaseAttributeKvEntry(
            System.currentTimeMillis(), new JsonDataEntry("streamOption", streamOption.toString()));
    attributesService.save(
        securityUser.getTenantId(),
        new DeviceId(cameraEditDto.getTbDeviceId()),
        DataConstants.SHARED_SCOPE,
        List.of(baseAttributeKvEntry));
    // Add tên thiết bị vào list deviceName
    boxService.saveDeviceNameToListDeviceName(
        cameraEditDto.getBoxId(), cameraEditDto.getTbDeviceId(), securityUser);

    checkNotNull(cameraEditDto);
    return cameraEditDto;
  }

  protected String getRtspUrl(String username, String password, String ip, int port, String type) {
    StringBuilder rtspUrlBuild = new StringBuilder();
    rtspUrlBuild
        .append("rtsp://")
        .append(username)
        .append(":")
        .append(password)
        .append("@")
        .append(ip)
        .append(":")
        .append(port)
        .append("/Streaming/Channels/");
    switch (type) {
      case MAIN_RTSP:
        rtspUrlBuild.append("101/");
        break;
      case SUB_RTSP:
        rtspUrlBuild.append("102/");
        break;
    }
    return rtspUrlBuild.toString();
  }

  protected String getOnvifUrl(String username, String password, String ip, int port) {
    return "http://" + username + ":" + password + "@" + ip + ":" + port + "/onvif/device_service";
  }

  protected String getLinkRtmp(String streamId) throws ThingsboardException, JsonProcessingException {
    SecurityUser securityUser = getCurrentUser();
    AdminSettings adminSettings =
        adminSettingsService.findAdminSettingsByKey(securityUser.getTenantId(), "live-stream");
    JsonNode settingsJsonValue = adminSettings.getJsonValue();
    AntMediaServerDto serverUrlDto = mapper.treeToValue(settingsJsonValue, AntMediaServerDto.class);
    String streamServerUrl = serverUrlDto.getHttpUrl();
    String server =
        streamServerUrl
            .replaceAll("http://", "")
            .replaceAll("https://", "")
            .replaceAll(":[0-9]*$", "");
    return "rtmp://" + server + "/" + streamId;
  }

  protected String getStreamIdFromRtmpUrl(String rtmpUrl) throws ThingsboardException {
    SecurityUser securityUser = getCurrentUser();
    AdminSettings adminSettings =
        adminSettingsService.findAdminSettingsByKey(securityUser.getTenantId(), "live-stream");
    String streamServerUrl = adminSettings.getJsonValue().get("url").asText();
    String server =
        streamServerUrl
            .replaceAll("http://", "")
            .replaceAll("https://", "")
            .replaceAll(":[0-9]*$", "");
    return rtmpUrl.replaceAll("rtmp://" + server + "/", "");
  }

  private DeviceProfile createNewDeviceProfile(
      CameraEditDto cameraEditDto, SecurityUser securityUser)
      throws JsonProcessingException, ThingsboardException {
    // Create new device profile cho camera
    DeviceProfile deviceProfile = new DeviceProfile();
    deviceProfile.setTenantId(securityUser.getTenantId());
    deviceProfile.setName(
        cameraEditDto.getCameraName()
            + "_DEVICEPROFILE_"
            + RandomStringUtils.randomAlphanumeric(5));
    DeviceProfileData deviceProfileData =
        mapper.readValue(
            "{\"configuration\":{\"type\":\"DEFAULT\"},\"transportConfiguration\":{\"type\":\"DEFAULT\"},\"alarms\":null,\"provisionConfiguration\":{\"type\":\"DISABLED\"}}",
            DeviceProfileData.class);
    deviceProfile.setProfileData(deviceProfileData);
    deviceProfile.setType(DeviceProfileType.DEFAULT);
    deviceProfile.setProvisionType(DeviceProfileProvisionType.DISABLED);
    deviceProfile.setTransportType(DeviceTransportType.DEFAULT);
    DeviceProfile savedDeviceProfile =
        checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfile));
    tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
    tbClusterService.broadcastEntityStateChangeEvent(
        deviceProfile.getTenantId(), savedDeviceProfile.getId(), ComponentLifecycleEvent.CREATED);

    Device device =
        deviceService.findDeviceById(
            securityUser.getTenantId(), new DeviceId(cameraEditDto.getTbDeviceId()));
    device.setDeviceProfileId(savedDeviceProfile.getId());
    Device savedDevice = deviceService.saveDevice(device);
    tbClusterService.onDeviceUpdated(savedDevice, null);
    tbClusterService.pushMsgToCore(
        new DeviceNameOrTypeUpdateMsg(
            savedDevice.getTenantId(),
            savedDevice.getId(),
            savedDevice.getName(),
            savedDevice.getType()),
        null);
    tbClusterService.broadcastEntityStateChangeEvent(
        savedDevice.getTenantId(), savedDevice.getId(), ComponentLifecycleEvent.UPDATED);
    return deviceProfile;
  }

  private String setAuthToRtspUrl(String rtspUrl, String rtspUsername, String rtspPassword) {
    return rtspUrl.replaceAll("rtsp://", "rtsp://" + rtspUsername + ":" + rtspPassword);
  }
}
