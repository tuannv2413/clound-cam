package org.thingsboard.server.dft.enduser.dao.camera;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.dft.enduser.dto.camera.CameraRecordSettingDto;
import org.thingsboard.server.dft.enduser.entity.camera.CameraRecordSettingEntity;
import org.thingsboard.server.dft.enduser.repository.camera.CameraRecordSettingRepository;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.*;

@Component
public class CameraRecordSettingDaoImpl implements CameraRecordSettingDao {

  private final CameraRecordSettingRepository cameraRecordSettingRepository;

  @Autowired
  public CameraRecordSettingDaoImpl(CameraRecordSettingRepository cameraRecordSettingRepository) {
    this.cameraRecordSettingRepository = cameraRecordSettingRepository;
  }

  @Override
  public CameraRecordSettingDto saveOrUpdate(CameraRecordSettingDto cameraRecordSettingDto, SecurityUser securityUser) throws ThingsboardException, JSONException, JsonProcessingException {
    CameraRecordSettingEntity cameraRecordSettingEntity = new CameraRecordSettingEntity();
    if (cameraRecordSettingDto.getId() != null) {
      Optional<CameraRecordSettingEntity> optionalCameraRecordSetting = cameraRecordSettingRepository.findById(cameraRecordSettingDto.getId());
      if (optionalCameraRecordSetting.isPresent()) {
        cameraRecordSettingEntity = optionalCameraRecordSetting.get();
        cameraRecordSettingEntity.setUpdatedBy(securityUser.getUuidId());
        cameraRecordSettingEntity.setUpdatedTime(new Date().getTime());
      } else {
        throw new ThingsboardException(ThingsboardErrorCode.ITEM_NOT_FOUND);
      }
    } else {
      cameraRecordSettingEntity.setId(UUID.randomUUID());
      cameraRecordSettingEntity.setCreatedBy(securityUser.getUuidId());
      cameraRecordSettingEntity.setCreatedTime(new Date().getTime());
    }
    cameraRecordSettingEntity.setTenantId(securityUser.getTenantId().getId());
    cameraRecordSettingEntity.setName(cameraRecordSettingDto.getName());
    cameraRecordSettingEntity.setCameraId(cameraRecordSettingDto.getCameraId());
    ObjectMapper mapper = new ObjectMapper();
    JsonNode setting = mapper.valueToTree(cameraRecordSettingDto.getSetting());
    cameraRecordSettingEntity.setSetting(setting);
    cameraRecordSettingEntity.setType(cameraRecordSettingDto.getType());
    cameraRecordSettingEntity.setActive(cameraRecordSettingDto.isActive());
    cameraRecordSettingEntity = cameraRecordSettingRepository.save(cameraRecordSettingEntity);
    return new CameraRecordSettingDto(cameraRecordSettingEntity);
  }

  @Override
  public List<CameraRecordSettingDto> getListCameraRecordSetting(SecurityUser securityUser) throws JSONException, JsonProcessingException {
    List<CameraRecordSettingEntity> cameraRecordSettingEntities = cameraRecordSettingRepository.findAllByTenantId(securityUser.getTenantId().getId());
    List<CameraRecordSettingDto> cameraRecordSettingDtos = new ArrayList<>();
    for (CameraRecordSettingEntity cameraRecordSettingEntity : cameraRecordSettingEntities) {
      CameraRecordSettingDto cameraRecordSettingDto = new CameraRecordSettingDto(cameraRecordSettingEntity);
      cameraRecordSettingDtos.add(cameraRecordSettingDto);
    }
    return cameraRecordSettingDtos;
  }

  @Override
  public CameraRecordSettingDto getByCameraId(UUID cameraId, SecurityUser securityUser) throws JSONException, JsonProcessingException {
    CameraRecordSettingEntity cameraRecordSettingEntity = cameraRecordSettingRepository.findByCameraIdAndTenantId(cameraId, securityUser.getTenantId().getId());
    if (cameraRecordSettingEntity != null) {
      return new CameraRecordSettingDto(cameraRecordSettingEntity);
    }
    return null;
  }

  @Override
  public void activeSetting(UUID id, boolean active, SecurityUser securityUser) throws ThingsboardException {
    Optional<CameraRecordSettingEntity> optionalCameraRecordSetting = cameraRecordSettingRepository.findById(id);
    if (optionalCameraRecordSetting.isPresent()) {
      CameraRecordSettingEntity cameraRecordSettingEntity = optionalCameraRecordSetting.get();
      cameraRecordSettingEntity.setActive(active);
      cameraRecordSettingRepository.save(cameraRecordSettingEntity);
    } else {
      throw new ThingsboardException(ThingsboardErrorCode.ITEM_NOT_FOUND);
    }
  }

  @Override
  public void deleteByCameraId(UUID cameraId, UUID tenantId) {
    cameraRecordSettingRepository.deleteByCameraIdAndTenantId(cameraId, tenantId);
  }

  @Override
  public void deleteByBoxId(UUID boxId, UUID tenantId) {
    cameraRecordSettingRepository.deleteByBoxIdAndTenantId(boxId, tenantId);
  }

  @Override
  public boolean checkExistNameByTenant(UUID tenantId, String name) {
    return cameraRecordSettingRepository.existsByNameAndTenantId(name, tenantId);
  }

  @Override
  public boolean checkExistNameByTenantAndIdNot(UUID tenantId, String name, UUID id) {
    return cameraRecordSettingRepository.existsByNameAndTenantIdAndIdNot(name, tenantId, id);
  }
}
