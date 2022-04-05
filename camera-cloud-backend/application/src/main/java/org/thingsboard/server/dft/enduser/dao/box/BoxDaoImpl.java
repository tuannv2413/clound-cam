package org.thingsboard.server.dft.enduser.dao.box;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dao.model.sql.AttributeKvCompositeKey;
import org.thingsboard.server.dao.model.sql.AttributeKvEntity;
import org.thingsboard.server.dao.sql.attributes.AttributeKvInsertRepository;
import org.thingsboard.server.dao.sql.attributes.AttributeKvRepository;
import org.thingsboard.server.dft.enduser.dto.box.BoxDetailDto;
import org.thingsboard.server.dft.enduser.dto.box.BoxEditDto;
import org.thingsboard.server.dft.enduser.entity.box.BoxEntity;
import org.thingsboard.server.dft.enduser.entity.camera.CameraEntity;
import org.thingsboard.server.dft.enduser.repository.box.BoxRepository;
import org.thingsboard.server.dft.enduser.repository.camera.CameraRepository;
import org.thingsboard.server.dft.mbgadmin.entity.mediaBox.MediaBoxEntity;
import org.thingsboard.server.dft.mbgadmin.repository.mediaBox.MediaBoxRepository;
import org.thingsboard.server.dft.util.constant.BoxInfoKeyConstant;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class BoxDaoImpl implements BoxDao {

  private final BoxRepository boxRepository;
  private final AttributeKvRepository attributeKvRepository;
  private final AttributeKvInsertRepository attributeKvInsertRepository;
  private final MediaBoxRepository mediaBoxRepository;
  private final CameraRepository cameraRepository;


  @Autowired
  public BoxDaoImpl(BoxRepository boxRepository, AttributeKvRepository attributeKvRepository,
                    AttributeKvInsertRepository attributeKvInsertRepository,
                    MediaBoxRepository mediaBoxRepository,
                    CameraRepository cameraRepository) {
    this.boxRepository = boxRepository;
    this.attributeKvRepository = attributeKvRepository;
    this.attributeKvInsertRepository = attributeKvInsertRepository;
    this.mediaBoxRepository = mediaBoxRepository;
    this.cameraRepository = cameraRepository;
  }

  @Override
  public BoxEditDto create(BoxEditDto box, UUID tbDeviceId, SecurityUser securityUser) throws JSONException {
    BoxEntity boxEntity = new BoxEntity();
    boxEntity.setId(tbDeviceId);
    boxEntity.setTenantId(securityUser.getTenantId().getId());
    boxEntity.setBoxName(box.getBoxName());
    boxEntity.setSerialNumber(box.getSerialNumber());
    boxEntity.setTbDeviceId(tbDeviceId);
    boxEntity.setCreatedTime(new Date().getTime());
    boxEntity.setCreatedBy(securityUser.getUuidId());

    MediaBoxEntity mediaBoxEntity = mediaBoxRepository.findMediaBoxEntityBySerialNumber(box.getSerialNumber());
    mediaBoxEntity.setUpdatedBy(securityUser.getUuidId());
    mediaBoxEntity.setUpdatedTime(new Date().getTime());
    boxEntity.setMediaBoxEntity(mediaBoxEntity);
    boxEntity = boxRepository.save(boxEntity);

    // Set trạng thái kết nối ban đầu của thiết bị
    List<String> listKey = attributeKvRepository.findAllKeysByEntityIds("DEVICE", List.of(tbDeviceId));
    if (!listKey.contains("ipv4")) {
      AttributeKvEntity attributeKIpv4Entity = new AttributeKvEntity();
      AttributeKvCompositeKey attributeKvCompositeKey = new AttributeKvCompositeKey();
      attributeKvCompositeKey.setAttributeKey("ipv4");
      attributeKvCompositeKey.setAttributeType(DataConstants.CLIENT_SCOPE);
      attributeKvCompositeKey.setEntityId(tbDeviceId);
      attributeKvCompositeKey.setEntityType(EntityType.DEVICE);
      attributeKIpv4Entity.setId(attributeKvCompositeKey);
      attributeKIpv4Entity.setStrValue("");
      attributeKIpv4Entity.setLastUpdateTs(new Date().getTime());
      attributeKvInsertRepository.saveOrUpdate(List.of(attributeKIpv4Entity));
    }
    if (!listKey.contains("model")) {
      AttributeKvEntity attributeModelEntity = new AttributeKvEntity();
      AttributeKvCompositeKey attributeKvCompositeKey = new AttributeKvCompositeKey();
      attributeKvCompositeKey.setAttributeKey("model");
      attributeKvCompositeKey.setAttributeType(DataConstants.CLIENT_SCOPE);
      attributeKvCompositeKey.setEntityId(tbDeviceId);
      attributeKvCompositeKey.setEntityType(EntityType.DEVICE);
      attributeModelEntity.setId(attributeKvCompositeKey);
      attributeModelEntity.setStrValue("");
      attributeModelEntity.setLastUpdateTs(new Date().getTime());
      attributeKvInsertRepository.saveOrUpdate(List.of(attributeModelEntity));
    }

    if (!listKey.contains("listDeviceName")) {
      AttributeKvEntity attributeModelEntity = new AttributeKvEntity();
      AttributeKvCompositeKey attributeKvCompositeKey = new AttributeKvCompositeKey();
      attributeKvCompositeKey.setAttributeKey(BoxInfoKeyConstant.LISTDEVICENAME);
      attributeKvCompositeKey.setAttributeType(DataConstants.SHARED_SCOPE);
      attributeKvCompositeKey.setEntityId(tbDeviceId);
      attributeKvCompositeKey.setEntityType(EntityType.DEVICE);
      attributeModelEntity.setId(attributeKvCompositeKey);

      JSONObject listDeviceNameJson = new JSONObject();
      listDeviceNameJson.put(BoxInfoKeyConstant.LISTDEVICENAME, new JSONArray());

      attributeModelEntity.setJsonValue(listDeviceNameJson.toString());
      attributeModelEntity.setLastUpdateTs(new Date().getTime());
      attributeKvInsertRepository.saveOrUpdate(List.of(attributeModelEntity));
    }

    return new BoxEditDto(boxEntity);
  }

  @Override
  public BoxEditDto update(BoxEditDto box, SecurityUser securityUser) {
    Optional<BoxEntity> optionalBoxEntity = boxRepository.findById(box.getId());
    if (optionalBoxEntity.isPresent()) {
      BoxEntity boxEntity = optionalBoxEntity.get();
      if (boxEntity.isDelete()) {
        return null;
      }
      boxEntity.setBoxName(box.getBoxName());
      boxEntity.setUpdatedTime(new Date().getTime());
      boxEntity.setUpdatedBy(securityUser.getUuidId());
      boxEntity = boxRepository.save(boxEntity);
      return new BoxEditDto(boxEntity);
    }
    return null;
  }

  @Override
  public PageData<BoxDetailDto> findAllBySearchText(Pageable pageable, Boolean status,
                                                    String searchText, SecurityUser securityUser,
                                                    boolean fullOption) {
    Page<BoxDetailDto> detailDtoPage;
    if (fullOption) {
      detailDtoPage = boxRepository
              .findAllBySearchInfo(pageable, status,
                      searchText, securityUser.getTenantId().getId())
              .map(BoxDetailDto::new);
    } else {
      detailDtoPage = boxRepository
              .findAllBySearchInfoCompact(pageable,
                      searchText, securityUser.getTenantId().getId())
              .map(BoxDetailDto::new);
    }
    return new PageData<>(detailDtoPage.getContent(), detailDtoPage.getTotalPages(),
        detailDtoPage.getTotalElements(), detailDtoPage.hasNext());
  }

  @Override
  public BoxDetailDto getBoxDetailById(UUID id) {
    Optional<BoxEntity> optionalBoxEntity = boxRepository.findById(id);
    if (optionalBoxEntity.isPresent()) {
      if (optionalBoxEntity.get().isDelete()) {
        return null;
      }
    }
    return optionalBoxEntity.map(BoxDetailDto::new).orElse(null);
  }

  @Override
  public BoxDetailDto getBoxDetailByTenantIdAndTbDeviceId(UUID tenantId, UUID tbDeviceId) {
    BoxEntity boxEntity = boxRepository.findByTenantIdAndTbDeviceIdAndIsDeleteFalse(tenantId, tbDeviceId);
    if (boxEntity != null) {
      return new BoxDetailDto(boxEntity);
    }
    return null;
  }

  @Override
  @Transactional
  public DeviceId deleteById(UUID id, UUID tenantId) {
    Optional<BoxEntity> optionalBoxEntity = boxRepository.findById(id);
    if (optionalBoxEntity.isPresent()) {
      BoxEntity boxEntity = optionalBoxEntity.get();
      if (boxEntity.isDelete()) {
        return null;
      }
      boxRepository.deleteSoftByIdAndTenantId(id, tenantId);
      for (CameraEntity cameraEntity : boxEntity.getCameraEntitySet()) {
        cameraRepository.deleteSoftByIdAndTenantId(cameraEntity.getId(), tenantId);
      }
      return new DeviceId(optionalBoxEntity.get().getTbDeviceId());
    }
    return null;
  }

  @Override
  public boolean checkExistSerialNumber(String serialNumber) {
    return boxRepository.existsBySerialNumberAndIsDeleteFalse(serialNumber);
  }

  @Override
  public List<BoxDetailDto> findAll(UUID tenantId, Sort sort) {
    return boxRepository.findAllByTenantIdAndIsDeleteFalse(tenantId, sort).stream().map(BoxDetailDto::new).collect(Collectors.toList());
  }

  @Override
  public boolean existsByBoxNameAndTenantId(String boxName, UUID tenantId) {
    return boxRepository.existsByBoxNameAndTenantIdAndIsDeleteFalse(boxName, tenantId);
  }

  @Override
  public boolean existsByBoxNameAndTenantIdAndIdNot(String boxName, UUID tenantId, UUID id) {
    return boxRepository.existsByBoxNameAndTenantIdAndIdNotAndIsDeleteFalse(boxName, tenantId, id);
  }
}
