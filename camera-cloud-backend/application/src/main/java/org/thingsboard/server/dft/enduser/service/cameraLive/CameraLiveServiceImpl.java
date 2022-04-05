package org.thingsboard.server.dft.enduser.service.cameraLive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.rpc.RpcError;
import org.thingsboard.server.common.data.rpc.ToDeviceRpcRequestBody;
import org.thingsboard.server.common.msg.rpc.FromDeviceRpcResponse;
import org.thingsboard.server.common.msg.rpc.ToDeviceRpcRequest;
import org.thingsboard.server.dao.settings.AdminSettingsService;
import org.thingsboard.server.dft.enduser.dao.camera.CameraDao;
import org.thingsboard.server.dft.enduser.dao.cameraLive.CameraLiveDao;
import org.thingsboard.server.dft.enduser.dto.camera.CameraEditDto;
import org.thingsboard.server.dft.enduser.dto.cameraLive.CameraStreamViewerDto;
import org.thingsboard.server.dft.util.service.AntMediaClient;
import org.thingsboard.server.service.rpc.TbCoreDeviceRpcService;
import org.thingsboard.server.service.security.AccessValidator;
import org.thingsboard.server.service.security.model.SecurityUser;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class CameraLiveServiceImpl implements CameraLiveService {

  protected final ObjectMapper mapper = new ObjectMapper();

  @Autowired
  protected TbCoreDeviceRpcService deviceRpcService;

  @Autowired
  protected AccessValidator accessValidator;

  @Value("${server.rest.server_side_rpc.min_timeout:5000}")
  protected long minTimeout;

  @Value("${server.rest.server_side_rpc.default_timeout:10000}")
  protected long defaultTimeout;

  @Autowired
  AntMediaClient antMediaClient;

  @Autowired
  AdminSettingsService adminSettingsService;

  private final CameraLiveDao cameraLiveDao;
  private final CameraDao cameraDao;
  private final TaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

  @Autowired
  public CameraLiveServiceImpl(CameraLiveDao cameraLiveDao,
                               CameraDao cameraDao) {
    this.cameraLiveDao = cameraLiveDao;
    this.cameraDao = cameraDao;
  }

  @Override
  public CameraStreamViewerDto saveToStreamViewer(UUID cameraId, SecurityUser securityUser) {
    return cameraLiveDao.saveViewerAndCamera(cameraId, securityUser);
  }

  @Override
  public boolean setCameraOnline(UUID cameraId, SecurityUser securityUser) throws ThingsboardException, JSONException, JsonProcessingException {
    CameraEditDto cameraEditDto = cameraDao.getCameraEditById(cameraId);
    if (cameraEditDto == null) {
      throw new ThingsboardException(ThingsboardErrorCode.ITEM_NOT_FOUND);
    }
    saveToStreamViewer(cameraId, securityUser);
    JSONObject streamObject = antMediaClient.getDetailsStreamById(cameraEditDto.getRtmpStreamId());
    if (streamObject != null) {
      String status = streamObject.getString("status");
      if (!status.equals("broadcasting")) {
        JSONObject valueRpc = new JSONObject();
        valueRpc.put("cameraId", cameraEditDto.getId());
        valueRpc.put("connectValue", "CONNECT");

        JSONObject requestParam = new JSONObject();
        requestParam.put("value", valueRpc);

        JSONObject requestBody = new JSONObject();
        requestBody.put("method", "STREAM_CONNECT");
        requestBody.put("params", requestParam);
        requestBody.put("retries", 5);
        handleDeviceRPCRequest(new DeviceId(cameraEditDto.getTbDeviceId()),
                requestBody.toString(), securityUser);
      }
    }
    return true;
  }

  @Override
  @Scheduled(fixedRate = 120000)
  public void checkStopStreamNoView() throws JSONException, JsonProcessingException, ThingsboardException {
    List<UUID> cameraIds = cameraLiveDao.getAllCameraIsViewing();
    for (UUID cameraId : cameraIds) {
      CameraEditDto cameraEditDto = cameraDao.getCameraEditById(cameraId);
      if (cameraEditDto != null) {
        JSONObject streamObject = antMediaClient.getDetailsStreamById(cameraEditDto.getRtmpStreamId());
        if (streamObject != null) {
          String status = streamObject.getString("status");
          int hlsViewer = streamObject.getInt("webRTCViewerCount");
          int webRtcViewer = streamObject.getInt("hlsViewerCount");
          int rtpmViewer = streamObject.getInt("rtmpViewerCount");
          if (!status.equals("broadcasting") || (hlsViewer + webRtcViewer + rtpmViewer) <= 0) {
            JSONObject valueRpc = new JSONObject();
            valueRpc.put("cameraId", cameraEditDto.getId());
            valueRpc.put("connectValue", "DISCONNECT");

            JSONObject requestParam = new JSONObject();
            requestParam.put("value", valueRpc);

            JSONObject requestBody = new JSONObject();
            requestBody.put("method", "STREAM_CONNECT");
            requestBody.put("params", requestParam);
            requestBody.put("retries", 5);
            SecurityUser securityUser = new SecurityUser();
            securityUser.setTenantId(new TenantId(cameraEditDto.getTenantId()));
            securityUser.setCustomerId(new CustomerId(CustomerId.NULL_UUID));
            handleDeviceRPCRequest(new DeviceId(cameraEditDto.getTbDeviceId()),
                requestBody.toString(), securityUser);
            cameraLiveDao.updateCameraStatusFalse(cameraId);
          }
        }
      }
    }
  }


  private void handleDeviceRPCRequest(DeviceId deviceId, String requestBody, SecurityUser currentUser)
      throws ThingsboardException {
    try {
      JsonNode rpcRequestBody = JacksonUtil.toJsonNode(requestBody);
      ToDeviceRpcRequestBody body = new ToDeviceRpcRequestBody(rpcRequestBody.get("method").asText(),
          JacksonUtil.toString(rpcRequestBody.get("params")));
      TenantId tenantId = currentUser.getTenantId();
      long timeout = rpcRequestBody.has(DataConstants.TIMEOUT) ?
          rpcRequestBody.get(DataConstants.TIMEOUT).asLong() : defaultTimeout;
      long expTime = rpcRequestBody.has(DataConstants.EXPIRATION_TIME) ?
          rpcRequestBody.get(DataConstants.EXPIRATION_TIME).asLong() : System.currentTimeMillis() + Math.max(minTimeout, timeout);
      UUID rpcRequestUUID = rpcRequestBody.has("requestUUID") ?
          UUID.fromString(rpcRequestBody.get("requestUUID").asText()) : UUID.randomUUID();
      boolean persisted = rpcRequestBody.has(DataConstants.PERSISTENT) &&
          rpcRequestBody.get(DataConstants.PERSISTENT).asBoolean();
      String additionalInfo = JacksonUtil.toString(rpcRequestBody.get(DataConstants.ADDITIONAL_INFO));
      Integer retries = rpcRequestBody.has(DataConstants.RETRIES) ? rpcRequestBody.get(DataConstants.RETRIES).asInt() : null;
      ToDeviceRpcRequest rpcRequest = new ToDeviceRpcRequest(rpcRequestUUID,
          tenantId,
          deviceId,
          true,
          expTime,
          body,
          persisted,
          retries,
          additionalInfo
      );
      deviceRpcService.processRestApiRpcRequest(rpcRequest, this::reply, currentUser);
    } catch (IllegalArgumentException ioe) {
      throw new ThingsboardException("Invalid request body", ioe, ThingsboardErrorCode.BAD_REQUEST_PARAMS);
    }
  }

  private void reply(FromDeviceRpcResponse response) {
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


}
