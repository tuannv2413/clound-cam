package org.thingsboard.server.dft.enduser.service.camera;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.dft.enduser.dao.camera.CameraRecordSettingDao;
import org.thingsboard.server.dft.enduser.dto.camera.CameraRecordSettingDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;

@Service
public class CameraRecordSettingServiceImpl implements CameraRecordSettingService{

  private final CameraRecordSettingDao cameraRecordSettingDao;

  @Autowired
  public CameraRecordSettingServiceImpl(CameraRecordSettingDao cameraRecordSettingDao) {
    this.cameraRecordSettingDao = cameraRecordSettingDao;
  }

  @Override
  public CameraRecordSettingDto saveOrUpdate(CameraRecordSettingDto cameraRecordSettingDto, SecurityUser securityUser) throws ThingsboardException, JSONException, JsonProcessingException {
    return cameraRecordSettingDao.saveOrUpdate(cameraRecordSettingDto, securityUser);
  }

  @Override
  public List<CameraRecordSettingDto> getListCameraRecordSetting(SecurityUser securityUser) throws JSONException, JsonProcessingException {
    return cameraRecordSettingDao.getListCameraRecordSetting(securityUser);
  }

  @Override
  public CameraRecordSettingDto getByCameraId(UUID cameraId, SecurityUser securityUser) throws JSONException, JsonProcessingException {
    return cameraRecordSettingDao.getByCameraId(cameraId, securityUser);
  }

  @Override
  public void activeSetting(UUID id, boolean active, SecurityUser securityUser) throws ThingsboardException {
    cameraRecordSettingDao.activeSetting(id, active, securityUser);
  }

  @Override
  public void deleteByCameraId(UUID cameraId, UUID tenantId) {
    cameraRecordSettingDao.deleteByCameraId(cameraId, tenantId);
  }

  @Override
  public void deleteByBoxId(UUID boxId, UUID tenantId) {
    cameraRecordSettingDao.deleteByBoxId(boxId, tenantId);
  }

  @Override
  public boolean checkExistNameByTenant(UUID tenantId, String name) {
    return cameraRecordSettingDao.checkExistNameByTenant(tenantId, name);
  }

  @Override
  public boolean checkExistNameByTenantAndIdNot(UUID tenantId, String name, UUID id) {
    return cameraRecordSettingDao.checkExistNameByTenantAndIdNot(tenantId, name, id);
  }
}
