package org.thingsboard.server.dft.enduser.service.camera;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.dft.enduser.dto.camera.CameraRecordSettingDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;

public interface CameraRecordSettingService {
  CameraRecordSettingDto saveOrUpdate(CameraRecordSettingDto cameraRecordSettingDto, SecurityUser securityUser) throws ThingsboardException, JSONException, JsonProcessingException;

  List<CameraRecordSettingDto> getListCameraRecordSetting(SecurityUser securityUser) throws JSONException, JsonProcessingException;

  CameraRecordSettingDto getByCameraId(UUID cameraId, SecurityUser securityUser) throws JSONException, JsonProcessingException;

  void activeSetting(UUID id, boolean active, SecurityUser securityUser) throws ThingsboardException;

  void deleteByCameraId(UUID cameraId, UUID tenantId);

  void deleteByBoxId(UUID boxId, UUID tenantId);

  boolean checkExistNameByTenant(UUID tenantId, String name);

  boolean checkExistNameByTenantAndIdNot(UUID tenantId, String name, UUID id);
}
