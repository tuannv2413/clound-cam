package org.thingsboard.server.dft.enduser.controller.camera;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.DeviceProfile;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.DeviceProfileId;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.dao.settings.AdminSettingsService;
import org.thingsboard.server.dft.enduser.controller.AbstractTelemetryController;
import org.thingsboard.server.dft.enduser.dto.box.BoxDetailDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDetailDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraEditDto;
import org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.CameraAdvancedSetting;
import org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.CameraSettingAttribute;
import org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.CameraUpdateAttribute;
import org.thingsboard.server.dft.util.constant.SystemConstant;
import org.thingsboard.server.dft.util.service.AntMediaClient;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;
import org.thingsboard.server.service.security.permission.Operation;

import javax.validation.Valid;
import java.net.URI;
import java.util.UUID;

import static org.thingsboard.server.controller.ControllerConstants.MARKDOWN_CODE_BLOCK_END;
import static org.thingsboard.server.controller.ControllerConstants.MARKDOWN_CODE_BLOCK_START;

@Slf4j
@RestController
@TbCoreComponent
@RequestMapping("/api/camera")
public class CameraController extends AbstractTelemetryController {

  private static final String MAIN_RTSP = "main_rtsp";
  private static final String SUB_RTSP = "sub_rtsp";

  @Autowired
  AntMediaClient antMediaClient;

  @Autowired
  AdminSettingsService adminSettingsService;

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @GetMapping
  @ResponseBody
  @ApiOperation(value = "Lấy danh sách camera có filter",
      notes = "Có thể sort theo một số trường sau created_time, camera_name"
  )
  public ResponseEntity<?> getAllByTextSearch(
      @RequestParam(name = "pageSize") int pageSize,
      @RequestParam(name = "page") int page,
      @RequestParam(required = false, defaultValue = "createdTime") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortOrder,
      @RequestParam(name = "boxId", required = false) UUID boxId,
      @RequestParam(required = false, defaultValue = "") String textSearch) throws ThingsboardException {
    try {
      textSearch = textSearch.trim();
      SecurityUser securityUser = getCurrentUser();
      Pageable pageable =
          PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty);
      return new ResponseEntity<>(checkNotNull(cameraService.getPageCameraBySearch(pageable, textSearch,
          boxId, securityUser)), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  //  @Transactional(rollbackFor = {Exception.class})
  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @GetMapping("{id}")
  @ResponseBody
  @ApiOperation(value = "Lấy thông chi tiết 1 camera")
  public ResponseEntity<?> getCameraEditById(@PathVariable("id") UUID id) throws ThingsboardException {
    try {
      return new ResponseEntity<>(checkNotNull(cameraService.getCameraDetailDtoById(id)), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @GetMapping("/scan-camera")
  @ResponseBody
  @ApiOperation(value = "Lấy danh sách camera scan từ box", notes = "Thực hiện sau ghi gọi lệnh scan box")
  public ResponseEntity<?> getAllDeviceInBox(@RequestParam(name = "boxId") UUID boxId) throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      return new ResponseEntity<>(checkNotNull(boxService.getListDeviceCameraInBox(boxId, securityUser)), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @ApiOperation(value = "Gửi các lệnh điều khiển xuống box",
      notes = "Type add: " +
          "\n\n AUTO: username và password onvif không được phép null " +
          "\n\n MANUAL: dường dẫn main rtsp không được phép null" +
          "\n\n Vd form request MANUAL: " +
          "\n\n" + MARKDOWN_CODE_BLOCK_START +
          "{\n" +
          "  \"boxId\": \"aaeb61f0-5aa1-11ec-a8a4-5f58e5686410\",\n" +
          "  \"cameraName\": \"Test add camera\",\n" +
          "  \"ipv4\": \"192.168.1.5\",\n" +
          "  \"mainRtspUrl\": \"rtsp://admin:admin@192.168.1.6:554/Streaming/Channels/101/\",\n" +
          "  \"subRtspurl\": \"rtsp://admin:admin@192.168.1.6:554/Streaming/Channels/102/\",\n" +
          "  \"onvifUrl\": \"http://admin:admin@192.168.1.244/onvif/device_service\"\n" +
          "}" +
          MARKDOWN_CODE_BLOCK_END +
          "\n\n Vd form request AUTO: " +
          "\n\n" + MARKDOWN_CODE_BLOCK_START +
          "{\n" +
          "  \"boxId\": \"aaeb61f0-5aa1-11ec-a8a4-5f58e5686410\",\n" +
          "  \"cameraName\": \"Test add camera\",\n" +
          "  \"ipv4\": \"192.168.1.5\",\n" +
          "  \"onvifPort\": \"8000\",\n" +
          "  \"onvifUsername\": \"admin\",\n" +
          "  \"onvifPassword\": \"admin\",\n" +
          "  \"rtspUsername\": \"admin\",\n" +
          "  \"rtspPassword\": \"admin\"\n" +
          "}" +
          MARKDOWN_CODE_BLOCK_END)
  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @RequestMapping(value = "/add-camera-v2/{typeAdd}/{boxId}", method = RequestMethod.POST)
  @ResponseBody
  public DeferredResult<ResponseEntity> sendConfigCameraToBoxAndAddCamera(
      @PathVariable("boxId") UUID boxId, @PathVariable("typeAdd") String typeAdd,
      @RequestBody @Valid CameraEditDto cameraEditDto) throws ThingsboardException {
    try {
      if (!typeAdd.equals(MANUAL) && !typeAdd.equals(AUTO)) {
        throw new ThingsboardException("Kiểu add camera không hợp lệ!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }

      if (typeAdd.equals(MANUAL)) {
        if (cameraEditDto.getMainRtspUrl() != null) {

          if (cameraEditDto.getMainRtspUrl().trim().equals("")) {
            throw new ThingsboardException("Đường dẫn stream không được phép rỗng!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
          } else {
            cameraEditDto.setMainRtspUrl(cameraEditDto.getMainRtspUrl().trim());
            extractRtsp(cameraEditDto);
          }

          if (cameraEditDto.getSubRtspUrl().trim().equals("")) {
            throw new ThingsboardException("Đường dẫn stream không được phép rỗng!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
          } else {
            cameraEditDto.setSubRtspUrl(cameraEditDto.getSubRtspUrl().trim());
          }

        } else {
          throw new ThingsboardException("Đường dẫn stream không được phép rỗng!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }

        if (cameraEditDto.getOnvifUrl() != null) {
          if (!cameraEditDto.getOnvifUrl().trim().equals("")) {
            cameraEditDto.setOnvifUrl(cameraEditDto.getOnvifUrl().trim());
            extractOnvif(cameraEditDto);
          }
        }
      } else {
        if (cameraEditDto.getOnvifUsername() == null || cameraEditDto.getOnvifPassword() == null) {
          throw new ThingsboardException("Tài khoản onvif là bắt buộc!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }
        if (cameraEditDto.getRtspUsername() == null || cameraEditDto.getRtspUsername().equals("")) {
          cameraEditDto.setRtspUsername(cameraEditDto.getOnvifUsername());
        }
        if (cameraEditDto.getRtspPassword() == null || cameraEditDto.getRtspPassword().equals("")) {
          cameraEditDto.setRtspPassword(cameraEditDto.getOnvifPassword());
        }
        cameraEditDto.setOnvifUrl(getOnvifUrl(cameraEditDto.getOnvifUsername(), cameraEditDto.getOnvifPassword(),
            cameraEditDto.getIpv4(), cameraEditDto.getOnvifPort()));
        cameraEditDto.setCameraName(cameraEditDto.getCameraName() + "_" + RandomStringUtils.randomAlphanumeric(12));
      }

      SecurityUser securityUser = getCurrentUser();
      cameraEditDto.setCameraName(cameraEditDto.getCameraName().trim());

      if (cameraService.existsByCameraNameAndTenantId(cameraEditDto.getCameraName(), securityUser.getTenantId().getId())) {
        throw new ThingsboardException("Tên đã tồn tài đã tồn tại!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }

      cameraEditDto.setId(UUID.randomUUID());
      checkDeviceId(new DeviceId(boxId), Operation.RPC_CALL);
      cameraEditDto.setId(UUID.randomUUID());
      if (cameraEditDto.getRtspPort() == 0) {
        cameraEditDto.setRtspPort(SystemConstant.DEFAULT_RTSP_PORT);
      }
      if (cameraEditDto.getOnvifPort() == 0) {
        cameraEditDto.setOnvifPort(SystemConstant.DEFAULT_ONVIF_PORT);
      }

      JSONObject antMediaStreamObject = antMediaClient.createStreamId(RandomStringUtils.randomAlphabetic(20));
      if (antMediaStreamObject == null) {
        throw new ThingsboardException("Tạo stream rtmp thất bại!", ThingsboardErrorCode.GENERAL);
      }
      cameraEditDto.setRtmpStreamId(antMediaStreamObject.getString("streamId"));
      cameraEditDto.setRtmpUrl(getLinkRtmp(cameraEditDto.getRtmpStreamId()));


      Gson gson = new Gson();
      String param = gson.toJson(cameraEditDto);
      JSONObject paramJson = new JSONObject(param);
      paramJson.remove("rtspPort");
      paramJson.remove("streamId");
      paramJson.remove("id");
      paramJson.remove("cameraName");
      paramJson.remove("boxId");
      paramJson.remove("onvifUrl");
      paramJson.put("cameraId", cameraEditDto.getId());
      paramJson.put("typeAdd", typeAdd);
      if (typeAdd.equals(MANUAL)) {
        paramJson.remove("fishEye");
        paramJson.remove("onvifPort");
        paramJson.remove("rtspUsername");
        paramJson.remove("rtspPassword");
      }
      JSONObject requestParam = new JSONObject();
      requestParam.put("value", paramJson);

      JSONObject requestBody = new JSONObject();
      requestBody.put("method", "ADD_CAMERA");
      requestBody.put("params", requestParam);
      log.info(requestBody.toString());

      return handleAddDeviceRPCRequest(false, new DeviceId(boxId),
          requestBody.toString(), HttpStatus.REQUEST_TIMEOUT, HttpStatus.CONFLICT, cameraEditDto, securityUser);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @ApiOperation(value = "Api change box camera v2")
  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @RequestMapping(value = "/change-box-v2/{cameraId}/{newBoxId}", method = RequestMethod.POST)
  @ResponseBody
  public DeferredResult<ResponseEntity> changeBoxCameraV2(
      @PathVariable("cameraId") UUID cameraId,
      @PathVariable("newBoxId") UUID newBoxId) throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      CameraEditDto cameraEditDto = cameraService.getCameraEditDtoById(cameraId);
      checkNotNull(cameraEditDto);

      Gson gson = new Gson();
      String param = gson.toJson(cameraEditDto);
      JSONObject paramJson = new JSONObject(param);
      paramJson.remove("rtspPort");
      paramJson.remove("streamId");
      paramJson.remove("rtspUsername");
      paramJson.remove("rtspPassword");
      paramJson.remove("id");
      paramJson.remove("cameraName");
      paramJson.remove("boxId");
      paramJson.remove("onvifUrl");
      paramJson.put("cameraId", cameraEditDto.getId());
      if (cameraEditDto.getOnvifUsername() == null || cameraEditDto.getOnvifUsername().trim().equals("")) {
        paramJson.put("typeAdd", MANUAL);
      } else {
        paramJson.put("typeAdd", AUTO);
      }

      JSONObject requestParam = new JSONObject();
      requestParam.put("value", paramJson);

      JSONObject requestBody = new JSONObject();
      requestBody.put("method", "ADD_CAMERA");
      requestBody.put("params", requestParam);
      requestBody.put("retries", 5);
      log.info(requestBody.toString());

      BoxDetailDto boxDetailDto = boxService.getBoxDetailById(newBoxId, securityUser);
      checkNotNull(boxDetailDto);

      return handleChangeBoxRPCRequest(false, new DeviceId(newBoxId),
          requestBody.toString(), HttpStatus.REQUEST_TIMEOUT, HttpStatus.CONFLICT, cameraEditDto, newBoxId, securityUser);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @PutMapping("{id}")
  @ResponseBody
  @ApiOperation(value = "Update thông tin camera",
      notes = "Vd form request: " +
          "\n\n" + MARKDOWN_CODE_BLOCK_START +
          "{\n" +
          "  \"id\": \"ce6525e0-5be0-11ec-9614-33414daa7efc\",\n" +
          "  \"cameraName\": \"Test add camera 2\",\n" +
          "  \"tbDeviceId\": \"bd0c2d30-64db-11ec-b2cb-9b01c5aa154e\",\n" +
          "  \"boxId\": \"bd0c2d30-64db-11ec-b2cb-9b01c5aa154e\",\n" +
          "  \"ipv4\": \"192.168.1.5\",\n" +
          "  \"mainRtspUrl\": \"rtsp://admin:admin@192.168.1.6:554/Streaming/Channels/101/\",\n" +
          "  \"subRtspUrl\": \"rtsp://admin:admin@192.168.1.6:554/Streaming/Channels/102/\",\n" +
          "  \"onvifUsername\": \"admin\",\n" +
          "  \"onvifPassword\": \"admin\",\n" +
          "  \"onvifPort\": 8000,\n" +
          "  \"channel\": \"mainStream\",\n" +
          "  \"resolutionSetting\": {\n" +
          "    \"width\": 1920,\n" +
          "    \"height\": 1080\n" +
          "  },\n" +
          "  \"fps\": 60,\n" +
          "  \"bitrate\": 8000,\n" +
          "  \"fisheye\": false\n" +
          "}" +
          MARKDOWN_CODE_BLOCK_END)
  public DeferredResult<ResponseEntity> updateCamera(@PathVariable("id") UUID cameraId,
                                                     @RequestBody CameraDetailDto cameraDetailDto) throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      CameraEditDto cameraEditDto = new CameraEditDto(cameraDetailDto);
      cameraEditDto.setId(cameraId);
      cameraEditDto.setCameraName(cameraEditDto.getCameraName().trim());
      cameraEditDto.setOnvifUrl(generateOnvifUrl(cameraEditDto));

      if (cameraService.existsByCameraNameAndTenantIdAndIdNot(cameraEditDto.getCameraName(), securityUser.getTenantId().getId(), cameraId)) {
        throw new ThingsboardException("Tên camera đã tồn tài đã tồn tại!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }

      if (cameraService.existsByTbDeviceIdAndTenantIdAndIdNot(cameraEditDto.getTbDeviceId(), securityUser.getTenantId().getId(), cameraId)) {
        throw new ThingsboardException("Thiết bị đã được kết nối với camera khác!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }

      if (cameraEditDto.getOnvifPort() == 0) {
        cameraEditDto.setOnvifPort(SystemConstant.DEFAULT_ONVIF_PORT);
      }

      CameraEditDto cameraEditDtoSaved = cameraService.update(cameraEditDto, securityUser);
      cameraEditDto.setTenantId(cameraEditDtoSaved.getTenantId());
      cameraEditDto.setTbDeviceId(cameraEditDtoSaved.getTbDeviceId());
      cameraEditDto.setRtmpStreamId(cameraEditDtoSaved.getRtmpStreamId());
      cameraEditDto.setRtmpUrl(cameraEditDtoSaved.getRtmpUrl());
      checkDeviceId(new DeviceId(cameraEditDtoSaved.getTbDeviceId()), Operation.WRITE_ATTRIBUTES);

      // Save attribute setting stream camera
      CameraSettingAttribute cameraSettingAttribute = new CameraSettingAttribute(cameraEditDto);
      CameraUpdateAttribute cameraUpdateAttribute = new CameraUpdateAttribute();
      cameraUpdateAttribute.setStreamOption(cameraSettingAttribute);
      JsonNode request = mapper.valueToTree(cameraUpdateAttribute);

      //Setting các tham số cài đặt camera
      CameraAdvancedSetting cameraAdvancedSetting = new CameraAdvancedSetting(cameraDetailDto);
      Gson gson = new Gson();
      String settingJson = gson.toJson(cameraAdvancedSetting);

      JSONObject valueRpc = new JSONObject();
      valueRpc.put("cameraId", cameraDetailDto.getId());
      valueRpc.put("settingValue", new JSONObject(settingJson));

      JSONObject requestParam = new JSONObject();
      requestParam.put("value", valueRpc);

      JSONObject requestBody = new JSONObject();
      requestBody.put("method", "SETTING_CAMERA");
      requestBody.put("params", requestParam);
      requestBody.put("retries", 5);

      handleDeviceRPCRequestNoResponse(new DeviceId(cameraEditDtoSaved.getTbDeviceId()), requestBody.toString(), securityUser);
      cameraLiveService.saveToStreamViewer(cameraId, securityUser);
      return saveAttributes(getTenantId(), new DeviceId(cameraEditDtoSaved.getTbDeviceId()), DataConstants.SHARED_SCOPE, request, cameraEditDto);

    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @DeleteMapping("{id}")
  @ResponseBody
  @ApiOperation(value = "Xóa camera by id (UUID)")
  public ResponseEntity<?> deleteById(@PathVariable("id") UUID id) throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      CameraEditDto cameraEditDto = cameraService.getCameraEditDtoById(id);
      checkNotNull(cameraEditDto);
      //delete ant media stream id
      antMediaClient.deleteStream(cameraEditDto.getRtmpStreamId());
//      cameraRecordSettingService.deleteByCameraId(cameraEditDto.getId(), getTenantId().getId());

      //xóa tên trong danh sách camera khởi tạo
      boxService.removeDeviceNameFromListDeviceName(cameraEditDto.getBoxId(),
          cameraEditDto.getTbDeviceId(), securityUser);

      //Xóa index trong group camera
      if (cameraEditDto.getCameraGroupId() != null) {
        cameraGroupService.deleteCameraInIndexSetting(securityUser, cameraEditDto.getId(),
            cameraEditDto.getCameraGroupId());
      }

      //delete camera
      cameraService.deleteById(getTenantId().getId(), id);

      //Thông báo xóa camera
      JSONObject valueRpc = new JSONObject();
      valueRpc.put("cameraId", id);
      JSONObject requestParam = new JSONObject();
      requestParam.put("value", valueRpc);

      JSONObject requestBody = new JSONObject();
      requestBody.put("method", "DELETE_CAMERA");
      requestBody.put("params", requestParam);
      requestBody.put("retries", 5);
      log.info(requestBody.toString());
      handleDeviceRPCRequestNoResponse(new DeviceId(cameraEditDto.getBoxId()), requestBody.toString(), securityUser);

      //delete device thingsboard tương ứng
      DeviceId deviceCameraId = new DeviceId(cameraEditDto.getTbDeviceId());
      Device device = checkDeviceId(deviceCameraId, Operation.DELETE);
      deviceService.deleteDevice(getCurrentUser().getTenantId(), deviceCameraId);
      tbClusterService.onDeviceDeleted(device, null);

      //delete deviceProfile tương ứng
      DeviceProfileId deviceProfileId = device.getDeviceProfileId();
      DeviceProfile deviceProfile = checkDeviceProfileId(deviceProfileId, Operation.DELETE);
      deviceProfileService.deleteDeviceProfile(getTenantId(), deviceProfileId);
      tbClusterService.onDeviceProfileDelete(deviceProfile, null);
      tbClusterService.broadcastEntityStateChangeEvent(deviceProfile.getTenantId(), deviceProfile.getId(), ComponentLifecycleEvent.DELETED);

      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  private void extractOnvif(CameraEditDto cameraEditDto) throws ThingsboardException {
    try {
      URI onvifUrl = new URI(cameraEditDto.getOnvifUrl());
      String userInfo = onvifUrl.getUserInfo();
      String[] userInfoArray = userInfo.split(":");
      cameraEditDto.setOnvifUsername(userInfoArray[0]);
      cameraEditDto.setOnvifPassword(userInfoArray[1]);
      cameraEditDto.setOnvifPort(onvifUrl.getPort() != -1 ? onvifUrl.getPort() : 80);
    } catch (Exception e) {
      throw new ThingsboardException("Đường dẫn Onvif không đúng định dạng",
          ThingsboardErrorCode.BAD_REQUEST_PARAMS);
    }
  }

  private void extractRtsp(CameraEditDto cameraEditDto) throws ThingsboardException {
    try {
      URI rtspUrl = new URI(cameraEditDto.getMainRtspUrl());
      String userInfo = rtspUrl.getUserInfo();
      if (rtspUrl.getUserInfo() != null || !rtspUrl.getUserInfo().equals("")) {
        String[] userInfoArray = userInfo.split(":");
        cameraEditDto.setRtspUsername(userInfoArray[0]);
        cameraEditDto.setRtspPassword(userInfoArray[1]);
      }
      cameraEditDto.setRtspPort(rtspUrl.getPort() != -1 ? rtspUrl.getPort() : 80);
      cameraEditDto.setIpv4(rtspUrl.getHost());
    } catch (Exception e) {
      throw new ThingsboardException("Đường dẫn rtsp không đúng định dạng",
          ThingsboardErrorCode.BAD_REQUEST_PARAMS);
    }
  }

  private String generateOnvifUrl(CameraEditDto cameraEditDto) {
    if (cameraEditDto.getOnvifUsername() != null) {
      return "http://" + cameraEditDto.getOnvifUsername() + ":" + cameraEditDto.getOnvifPassword()
          + "@" + cameraEditDto.getIpv4() + ":" + cameraEditDto.getOnvifPort() + "/onvif/device_service";
    }
    return null;
  }

}
