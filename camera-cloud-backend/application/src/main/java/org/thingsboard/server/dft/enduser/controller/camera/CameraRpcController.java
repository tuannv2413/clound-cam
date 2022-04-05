package org.thingsboard.server.dft.enduser.controller.camera;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.dft.enduser.controller.AbstractSendRpcController;
import org.thingsboard.server.dft.enduser.dto.camera.CameraEditDto;
import org.thingsboard.server.dft.enduser.dto.user.EndUserDto;
import org.thingsboard.server.dft.mbgadmin.service.clTenant.CLTenantService;
import org.thingsboard.server.dft.util.service.AntMediaClient;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.UUID;

@Slf4j
@RestController
@TbCoreComponent
@RequestMapping("/api/camera/send-rpc")
public class CameraRpcController extends AbstractSendRpcController {

  @Autowired
  AntMediaClient antMediaClient;

  @Autowired
  CLTenantService clTenantService;

  @ApiOperation(value = "Gửi các lệnh điều khiển xuống box",
      notes = "0 : stop\n\n" +
          "1: up\n\n 2 down\n\n 3 left\n\n 4 right\n\n 5 left up\n\n 6 left down\n\n" +
          "7 right up\n\n 8 right down\n\n 9 zoom out\n\n 10 zoom in\n\n 11 reset zoom\n\n" +
          "12 fisheye\n\n 13 reboot camera")
  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @RequestMapping(value = "/ptz/{id}/{valueControl}", method = RequestMethod.POST)
  @ResponseBody
  public DeferredResult<ResponseEntity> handlePtzCameraRequest(
      @PathVariable("id") UUID id,
      @PathVariable("valueControl") Integer valueControl) throws ThingsboardException, JSONException {
    try {
      SecurityUser securityUser = new SecurityUser();
      CameraEditDto cameraEditDto = cameraService.getCameraEditDtoById(id);
      boolean isTenantAdmin = true;
      boolean checkUserExist = endUserService.existsByTenantIdAndUserId(getTenantId().getId(), getCurrentUser().getUuidId());
      if (checkUserExist) {
        EndUserDto endUserDto = endUserService.getByUserId(getCurrentUser().getUuidId(), securityUser);
        isTenantAdmin = clTenantService.isTenantUser(endUserDto.getId());
      }
      if (!customerCameraPermissionService.checkHistoryPermission(securityUser, id) && !isTenantAdmin) {
        throw new ThingsboardException("Bạn không có quyền điều khiển camera này!",
            ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }
      checkNotNull(cameraEditDto);
      JSONObject valueRpc = new JSONObject();
      valueRpc.put("cameraId", id);
      valueRpc.put("ptzValue", valueControl);

      JSONObject requestParam = new JSONObject();
      requestParam.put("value", valueRpc);

      JSONObject requestBody = new JSONObject();
      requestBody.put("method", "PTZ");
      requestBody.put("params", requestParam);
      requestBody.put("retries", 5);
      log.info(requestBody.toString());
      return handleDeviceRPCRequest(true, new DeviceId(cameraEditDto.getTbDeviceId()),
          requestBody.toString(), HttpStatus.REQUEST_TIMEOUT, HttpStatus.CONFLICT);
    } catch (Exception e) {
      throw handleException(e);
    }
  }


}
