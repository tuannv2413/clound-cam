package org.thingsboard.server.dft.enduser.controller.camera;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.dft.enduser.controller.AbstractTelemetryController;
import org.thingsboard.server.dft.enduser.dto.camera.CameraEditDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraRecordSettingDto;
import org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.SettingRecordGroupAttribute;
import org.thingsboard.server.dft.enduser.entity.camera.recordSetting.RecordSetting;
import org.thingsboard.server.dft.util.constant.CameraInfoKeyConstant;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import javax.annotation.security.PermitAll;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api/camera-record-setting")
public class CameraRecordSettingController extends AbstractTelemetryController {

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @GetMapping("/all")
  @ResponseBody
  @ApiOperation(value = "Lấy toàn bộ danh sách setting lưu trữ")
  public ResponseEntity<?> getAll() throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      return new ResponseEntity<>(checkNotNull(cameraRecordSettingService.getListCameraRecordSetting(securityUser)), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @GetMapping("{cameraId}")
  @ResponseBody
  @ApiOperation(value = "Lấy toàn bộ danh sách setting lưu trữ")
  public ResponseEntity<?> getById(@PathVariable("cameraId") UUID cameraId) throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      return new ResponseEntity<>(cameraRecordSettingService.getByCameraId(cameraId, securityUser), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }


  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @PostMapping
  @ResponseBody
  @ApiOperation(value = "Save cấu hình lưu trữ của camera",
      notes = "type sẽ gồm 3 loại: FULL_TIME: lưu 0h - 23h59 các ngày, " +
          "CUSTOM_TIME: tự ng dùng chọn, " +
          "COPY_TIME: copy từ setting khác")
  public DeferredResult<ResponseEntity> saveOrUpdate(@RequestBody CameraRecordSettingDto cameraRecordSettingDto) throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      CameraEditDto cameraEditDto = cameraService.getCameraEditDtoById(cameraRecordSettingDto.getCameraId());
      checkNotNull(cameraEditDto);

      if (!cameraRecordSettingDto.getType().equals(CameraInfoKeyConstant.FULL_TIME) &&
          !cameraRecordSettingDto.getType().equals(CameraInfoKeyConstant.COPY_TIME) &&
          !cameraRecordSettingDto.getType().equals(CameraInfoKeyConstant.CUSTOM_TIME)) {
        throw new ThingsboardException("Không tìm thấy kiểu setting!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }

      if (cameraRecordSettingDto.getType().equals(CameraInfoKeyConstant.FULL_TIME)) {
        cameraRecordSettingDto.setSetting(new RecordSetting(CameraInfoKeyConstant.FULL_TIME));
      }

      if (cameraRecordSettingDto.getId() != null) {
        if (cameraRecordSettingService.checkExistNameByTenantAndIdNot(securityUser.getTenantId().getId(),
            cameraRecordSettingDto.getName(), cameraRecordSettingDto.getId())) {
          throw new ThingsboardException("Tên đã tồn tài đã tồn tại!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }
      } else {
        if (cameraRecordSettingService.checkExistNameByTenant(securityUser.getTenantId().getId(), cameraRecordSettingDto.getName())) {
          throw new ThingsboardException("Tên đã tồn tài đã tồn tại!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }
        if (cameraRecordSettingService.getByCameraId(cameraEditDto.getId(), securityUser) != null) {
          throw new ThingsboardException("Camera đã tồn tại record setting!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }
      }

      cameraRecordSettingDto = cameraRecordSettingService.saveOrUpdate(cameraRecordSettingDto, securityUser);

      SettingRecordGroupAttribute settingRecordAttribute = new SettingRecordGroupAttribute();
      settingRecordAttribute.setCameraId(cameraRecordSettingDto.getCameraId());
      if (cameraRecordSettingDto.isActive()) {
        settingRecordAttribute.setRecordSetting(cameraRecordSettingDto.getSetting());
      } else {
        settingRecordAttribute.setRecordSetting(new RecordSetting(CameraInfoKeyConstant.INACTIVE_TIME));
      }
      JsonNode request = mapper.valueToTree(settingRecordAttribute);

      return saveAttributes(getTenantId(), new DeviceId(cameraEditDto.getTbDeviceId()), DataConstants.SHARED_SCOPE, request, cameraRecordSettingDto);

    } catch (Exception e) {
      throw handleException(e);
    }
  }

}
