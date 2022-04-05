package org.thingsboard.server.dft.enduser.dao.camera;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dao.model.sql.AdminSettingsEntity;
import org.thingsboard.server.dao.sql.settings.AdminSettingsRepository;
import org.thingsboard.server.dft.enduser.dto.camera.AddCameraGroupDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDetailDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraEditDto;
import org.thingsboard.server.dft.enduser.dto.cameraGroup.CameraStreamGroup;
import org.thingsboard.server.dft.enduser.entity.box.BoxEntity;
import org.thingsboard.server.dft.enduser.entity.camera.CameraEntity;
import org.thingsboard.server.dft.enduser.entity.cameraGroup.CameraGroupEntity;
import org.thingsboard.server.dft.enduser.repository.box.BoxRepository;
import org.thingsboard.server.dft.enduser.repository.camera.CameraRepository;
import org.thingsboard.server.dft.enduser.repository.cameraGroup.CameraGroupRepository;
import org.thingsboard.server.dft.mbgadmin.dto.setting.AntMediaServerDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CameraDaoImpl implements CameraDao {

  private final CameraRepository cameraRepository;
  private final BoxRepository boxRepository;
  private final CameraGroupRepository cameraGroupRepository;
  private final AdminSettingsRepository adminSettingsRepository;

  @Autowired
  public CameraDaoImpl(
      CameraRepository cameraRepository,
      BoxRepository boxRepository,
      CameraGroupRepository cameraGroupRepository,
      AdminSettingsRepository adminSettingsRepository) {
    this.cameraRepository = cameraRepository;
    this.boxRepository = boxRepository;
    this.cameraGroupRepository = cameraGroupRepository;
    this.adminSettingsRepository = adminSettingsRepository;
  }

  @Transactional
  @Override
  public PageData<CameraDto> getPageCamera(
      Pageable pageable, String searchText, UUID boxId, UUID tenantId) {
    Page<CameraEntity> cameraEntityPage;
    if (boxId == null) {
      cameraEntityPage = cameraRepository.findAllBySearchInfo(pageable, searchText, tenantId);
    } else {
      cameraEntityPage =
          cameraRepository.findAllBySearchInfo(pageable, boxId, searchText, tenantId);
    }
    Page<CameraDto> cameraDtoPage = cameraEntityPage.map(CameraDto::new);
    for (CameraDto cameraDto : cameraDtoPage.getContent()) {
      CameraGroupEntity cameraGroupEntity =
          cameraGroupRepository.findByTenantIdAndId(tenantId, cameraDto.getGroupId());
      if (cameraGroupEntity != null) {
        cameraDto.setGroupName(cameraGroupEntity.getCameraGroupName());
      }
    }
    return new PageData<>(
        cameraDtoPage.getContent(),
        cameraEntityPage.getTotalPages(),
        cameraDtoPage.getTotalElements(),
        cameraDtoPage.hasNext());
  }

  @Transactional
  @Override
  public List<CameraStreamGroup> getAllCameraWithGroup(String searchText, SecurityUser securityUser)
      throws JsonProcessingException {
    List<CameraStreamGroup> cameraStreamGroups = new ArrayList<>();
    AdminSettingsEntity adminSettingsEntity = adminSettingsRepository.findByKey("live-stream");
    JsonNode streamJsonNode = adminSettingsEntity.getJsonValue();
    ObjectMapper mapper = new ObjectMapper();
    AntMediaServerDto antMediaServerDto =
        mapper.treeToValue(streamJsonNode, AntMediaServerDto.class);
    // Get all camera khong co group
    CameraStreamGroup cameraStreamNoGroup = new CameraStreamGroup();
    cameraStreamNoGroup.setGroupId(null);
    cameraStreamNoGroup.setGroupName("Chưa có group");
    List<CameraDetailDto> cameraDetailDtoList =
        cameraRepository.findByCameraGroupIdIsNullAndIsDeleteFalseOrderByCreatedTimeDesc().stream()
            .filter(
                cameraEntity ->
                    cameraEntity.getCameraName().toLowerCase().contains(searchText.toLowerCase()))
            .map(cameraEntity -> new CameraDetailDto(cameraEntity, antMediaServerDto))
            .collect(Collectors.toList());
    cameraStreamNoGroup.setListCamera(cameraDetailDtoList);
    cameraStreamGroups.add(cameraStreamNoGroup);

    // Get all camera da co group
    List<CameraGroupEntity> cameraGroupEntities =
        cameraGroupRepository.findAllByTenantId(securityUser.getTenantId().getId());
    for (CameraGroupEntity cameraGroupEntity : cameraGroupEntities) {
      CameraStreamGroup cameraStreamGroup = new CameraStreamGroup();
      cameraStreamGroup.setGroupId(cameraGroupEntity.getId());
      cameraStreamGroup.setGroupName(cameraGroupEntity.getCameraGroupName());
      List<CameraDetailDto> cameraEntityList =
          cameraRepository
              .findByCameraGroupWithSearch(cameraGroupEntity.getId(), searchText)
              .stream()
              .map(cameraEntity -> new CameraDetailDto(cameraEntity, antMediaServerDto))
              .collect(Collectors.toList());
      cameraStreamGroup.setListCamera(cameraEntityList);
      cameraStreamGroups.add(cameraStreamGroup);
    }
    return cameraStreamGroups;
  }

  @Transactional
  @Override
  public CameraStreamGroup getCameraStreamByGroup(
      UUID groupId, String searchText, SecurityUser securityUser) throws JsonProcessingException {
    CameraStreamGroup cameraStreamGroup = new CameraStreamGroup();
    AdminSettingsEntity adminSettingsEntity = adminSettingsRepository.findByKey("live-stream");
    JsonNode streamJsonNode = adminSettingsEntity.getJsonValue();
    ObjectMapper mapper = new ObjectMapper();
    AntMediaServerDto serverUrlDto = mapper.treeToValue(streamJsonNode, AntMediaServerDto.class);
    if (groupId != null) {
      CameraGroupEntity cameraGroupEntity =
          cameraGroupRepository.findByTenantIdAndId(securityUser.getTenantId().getId(), groupId);
      if (cameraGroupEntity != null) {
        cameraStreamGroup.setGroupId(cameraGroupEntity.getId());
        cameraStreamGroup.setGroupName(cameraGroupEntity.getCameraGroupName());
        List<CameraDetailDto> cameraEntityList =
            cameraRepository.findByCameraGroup(groupId).stream()
                .filter(
                    cameraEntity ->
                        cameraEntity
                            .getCameraName()
                            .toLowerCase()
                            .contains(searchText.toLowerCase()))
                .map(cameraEntity -> new CameraDetailDto(cameraEntity, serverUrlDto))
                .collect(Collectors.toList());
        cameraStreamGroup.setListCamera(cameraEntityList);
      } else {
        return null;
      }
    } else {
      cameraStreamGroup.setGroupId(null);
      cameraStreamGroup.setGroupName("Chưa có group");
      List<CameraDetailDto> cameraDetailDtoList =
          cameraRepository
              .findByCameraGroupIdIsNullAndIsDeleteFalseOrderByCreatedTimeDesc()
              .stream()
              .filter(
                  cameraEntity ->
                      cameraEntity.getCameraName().toLowerCase().contains(searchText.toLowerCase()))
              .map(cameraEntity -> new CameraDetailDto(cameraEntity, serverUrlDto))
              .collect(Collectors.toList());
      cameraStreamGroup.setListCamera(cameraDetailDtoList);
    }
    return cameraStreamGroup;
  }

  @Override
  public CameraEditDto save(CameraEditDto cameraEditDto, SecurityUser securityUser) {
    Optional<BoxEntity> optionalBoxEntity = boxRepository.findById(cameraEditDto.getBoxId());
    if (optionalBoxEntity.isPresent()) {
      if (optionalBoxEntity.get().isDelete()) {
        return null;
      }
      CameraEntity cameraEntity = new CameraEntity();
      cameraEntity.setId(cameraEditDto.getId());
      cameraEntity.setBoxEntity(optionalBoxEntity.get());
      cameraEntity.setCameraName(cameraEditDto.getCameraName());
      cameraEntity.setIpv4(cameraEditDto.getIpv4());
      cameraEntity.setRtspPort(cameraEditDto.getRtspPort());
      cameraEntity.setRtspUsername(cameraEditDto.getRtspUsername());
      cameraEntity.setRtspPassword(cameraEditDto.getRtspPassword());
      cameraEntity.setMainRtspUrl(cameraEditDto.getMainRtspUrl());
      cameraEntity.setSubRtspUrl(cameraEditDto.getSubRtspUrl());

      cameraEntity.setOnvifPort(cameraEditDto.getOnvifPort());
      cameraEntity.setOnvifUsername(cameraEditDto.getOnvifUsername());
      cameraEntity.setOnvifPassword(cameraEditDto.getOnvifPassword());
      cameraEntity.setOnvifUrl(cameraEditDto.getOnvifUrl());
      cameraEntity.setRtmpStreamId(cameraEditDto.getRtmpStreamId());
      cameraEntity.setRtmpUrl(cameraEditDto.getRtmpUrl());
      cameraEntity.setCameraFisheye(cameraEditDto.isFishEye());
      cameraEntity.setTbDeviceId(cameraEditDto.getTbDeviceId());

      cameraEntity.setTenantId(securityUser.getTenantId().getId());
      cameraEntity.setCreatedTime(new Date().getTime());
      cameraEntity.setCreatedBy(securityUser.getUuidId());

      cameraEntity = cameraRepository.save(cameraEntity);
      return new CameraEditDto(cameraEntity);
    }
    return null;
  }

  @Override
  public CameraEditDto update(CameraEditDto cameraEditDto, SecurityUser securityUser) {
    Optional<CameraEntity> optionalCameraEntity = cameraRepository.findById(cameraEditDto.getId());
    if (optionalCameraEntity.isPresent()) {
      CameraEntity cameraEntity = optionalCameraEntity.get();
      cameraEntity.setId(cameraEditDto.getId());
      cameraEntity.setCameraName(cameraEditDto.getCameraName());
      cameraEntity.setIpv4(cameraEditDto.getIpv4());
      cameraEntity.setRtspPort(cameraEditDto.getRtspPort());
      cameraEntity.setRtspUsername(cameraEditDto.getRtspUsername());
      cameraEntity.setRtspPassword(cameraEditDto.getRtspPassword());
      cameraEntity.setMainRtspUrl(cameraEditDto.getMainRtspUrl());
      cameraEntity.setSubRtspUrl(cameraEditDto.getSubRtspUrl());

      cameraEntity.setOnvifPort(cameraEditDto.getOnvifPort());
      cameraEntity.setOnvifUsername(cameraEditDto.getOnvifUsername());
      cameraEntity.setOnvifPassword(cameraEditDto.getOnvifPassword());
      cameraEntity.setOnvifUrl(cameraEditDto.getOnvifUrl());
      cameraEntity.setCameraFisheye(cameraEditDto.isFishEye());

      cameraEntity.setUpdatedTime(new Date().getTime());
      cameraEntity.setUpdatedBy(securityUser.getUuidId());

      cameraEntity = cameraRepository.save(cameraEntity);
      return new CameraEditDto(cameraEntity);
    }
    return null;
  }

  @Override
  public CameraEditDto changeBox(CameraEditDto cameraEditDto, SecurityUser securityUser) {
    Optional<CameraEntity> optionalCameraEntity = cameraRepository.findById(cameraEditDto.getId());
    Optional<BoxEntity> optionalBoxEntity = boxRepository.findById(cameraEditDto.getBoxId());
    if (optionalCameraEntity.isPresent() && optionalBoxEntity.isPresent()) {
      if (optionalBoxEntity.get().isDelete() || optionalCameraEntity.get().isDelete()) {
        return null;
      }
      CameraEntity cameraEntity = optionalCameraEntity.get();
      cameraEntity.setBoxEntity(optionalBoxEntity.get());
      cameraEntity.setTbDeviceId(cameraEditDto.getTbDeviceId());
      cameraEntity.setUpdatedTime(new Date().getTime());
      cameraEntity.setUpdatedBy(securityUser.getUuidId());

      cameraEntity = cameraRepository.save(cameraEntity);
      return new CameraEditDto(cameraEntity);
    }
    return null;
  }

  @Override
  @Transactional
  public CameraEditDto getCameraEditById(UUID id) {
    CameraEntity cameraEntity = cameraRepository.findById(id).orElse(null);
    if (cameraEntity != null) {
      if (cameraEntity.isDelete()) {
        return null;
      }
      return new CameraEditDto(cameraEntity);
    }
    return null;
  }

  @Override
  public CameraEditDto getCameraEditByTenantIdAndTbDeviceId(UUID tenantId, UUID tbDeviceId) {
    CameraEntity cameraEntity = cameraRepository.findByTenantIdAndTbDeviceId(tenantId, tbDeviceId);
    if (cameraEntity != null) {
      if (cameraEntity.isDelete()) {
        return null;
      }
      return new CameraEditDto(cameraEntity);
    }
    return null;
  }

  @Override
  @Transactional
  public CameraDetailDto getCameraDetailDtoById(UUID id) {
    CameraEntity cameraEntity = cameraRepository.findById(id).orElse(null);
    if (cameraEntity != null) {
      if (cameraEntity.isDelete()) {
        return null;
      }
      return new CameraDetailDto(cameraEntity);
    }
    return null;
  }

  @Override
  public void deleteById(UUID tenantId, UUID id) {
    cameraRepository.deleteSoftByIdAndTenantId(id, tenantId);
  }

  @Transactional
  @Override
  public void deleteByBoxId(UUID tenantId, UUID boxId) {
    cameraRepository.deleteByTenantIdAndBoxId(tenantId, boxId);
  }

  @Override
  public boolean existsByTbDeviceIdAndTenantId(UUID tbDeviceId, UUID tenantId) {
    return cameraRepository.existsByTbDeviceIdAndTenantIdAndIsDeleteFalse(tbDeviceId, tenantId);
  }

  @Transactional
  @Override
  public boolean existsByTbDeviceIdAndTenantIdAndBoxId(UUID tbDeviceId, UUID tenantId, UUID boxId) {
    return cameraRepository
        .existsByTbDeviceIdAndTenantIdAndBoxIdAndIsDeleteFalse(tbDeviceId, tenantId, boxId);
  }

  @Override
  public boolean existsByTbDeviceIdAndTenantIdAndIdNot(UUID tbDeviceId, UUID tenantId, UUID id) {
    return cameraRepository.existsByTbDeviceIdAndTenantIdAndIdNotAndIsDeleteFalse(
        tbDeviceId, tenantId, id);
  }

  @Override
  public boolean existsByCameraNameAndTenantId(String cameraName, UUID tenantId) {
    return cameraRepository.existsByCameraNameAndTenantIdAndIsDeleteFalse(cameraName, tenantId);
  }

  @Override
  public boolean existsByCameraNameAndTenantIdAndIdNot(String cameraName, UUID tenantId, UUID id) {
    return cameraRepository.existsByCameraNameAndTenantIdAndIdNotAndIsDeleteFalse(
        cameraName, tenantId, id);
  }

  @Override
  public void deleteFromGroup(UUID tenantId, UUID id) {
    CameraEntity cameraEntity = cameraRepository.findByTenantIdAndId(tenantId, id);
    cameraEntity.setCameraGroupId(null);
    cameraRepository.save(cameraEntity);
  }

  @Override
  public void addToGroup(UUID tenantId, UUID id, AddCameraGroupDto addCameraGroupDto) {
    CameraEntity cameraEntity = cameraRepository.findByTenantIdAndId(tenantId, id);
    CameraGroupEntity cameraGroupEntity =
        cameraGroupRepository.findById(addCameraGroupDto.getCameraGroupId()).orElse(null);

    cameraEntity.setCameraGroupId(addCameraGroupDto.getCameraGroupId());
    cameraRepository.save(cameraEntity);
  }

  @Override
  public CameraDetailDto existByBoxIdAndIpv4(UUID boxId, String ipv4) {
    CameraEntity cameraEntity = cameraRepository.existsByBoxIdAndIpv4(boxId, ipv4);
    if (cameraEntity != null) {
      return new CameraDetailDto(cameraEntity);
    }
    return null;
  }
}
