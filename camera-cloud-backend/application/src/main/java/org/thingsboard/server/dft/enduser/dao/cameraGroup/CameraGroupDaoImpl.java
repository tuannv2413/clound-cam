package org.thingsboard.server.dft.enduser.dao.cameraGroup;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDetailDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDto;
import org.thingsboard.server.dft.enduser.dto.cameraGroup.*;
import org.thingsboard.server.dft.enduser.entity.camera.CameraEntity;
import org.thingsboard.server.dft.enduser.entity.cameraGroup.CameraGroupEntity;
import org.thingsboard.server.dft.enduser.entity.user.EndUserEntity;
import org.thingsboard.server.dft.enduser.repository.camera.CameraRepository;
import org.thingsboard.server.dft.enduser.repository.cameraGroup.CameraGroupRepository;
import org.thingsboard.server.dft.enduser.repository.user.EndUserRepository;
import org.thingsboard.server.dft.enduser.repository.user.TBUserRepository;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CameraGroupDaoImpl implements CameraGroupDao {

    @Autowired
    private EndUserRepository endUserRepository;
    @Autowired
    private CameraRepository cameraRepository;
    @Autowired
    private TBUserRepository tbUserRepository;


    private final CameraGroupRepository cameraGroupRepository;

    public CameraGroupDaoImpl(CameraGroupRepository cameraGroupRepository) {
        this.cameraGroupRepository = cameraGroupRepository;
    }

    @Override
    @Transactional
    public CameraGroupResponse save(CameraGroupDto cameraGroupDto, SecurityUser securityUser) throws ThingsboardException {
        if (cameraGroupDto.getId() != null) {
            if (cameraGroupRepository.findById(cameraGroupDto.getId()).isEmpty()) {
                throw new ThingsboardException("Nhóm không tồn tại!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
            }
            return saveOrUpdate(cameraGroupDto.getId(), cameraGroupDto, securityUser);
        } else {
            return saveOrUpdate(null, cameraGroupDto, securityUser);
        }
    }

    @Transactional
    void refreshCameraAfterAddOrUpdateCamGroup(CameraGroupEntity cameraGroupEntity, List<CameraIdDto> cameraIdList, SecurityUser securityUser) {
        List<CameraEntity> cameraEntities = cameraRepository.findByCameraGroup(cameraGroupEntity.getId());
        List<CameraIdDto> existedCameraIdDtos = new ArrayList<>();
        for (CameraEntity cameraEntity : cameraEntities) {
            CameraIdDto cameraIdDto = new CameraIdDto();
            cameraIdDto.setCameraId(cameraEntity.getId());
            existedCameraIdDtos.add(cameraIdDto);
        }
        if (!existedCameraIdDtos.equals(cameraIdList)) {
            if (!cameraIdList.isEmpty()) {
                for (CameraIdDto cameraId : cameraIdList) {
                    CameraEntity cameraEntity = cameraRepository.findById(cameraId.getCameraId()).orElse(null);
                    if (cameraEntity != null) {
                        if (cameraEntity.getCameraGroupId() != cameraGroupEntity.getId() && cameraEntity.getCameraGroupId() != null) {
                            CameraGroupEntity oldGroup = cameraGroupRepository.findById(cameraEntity.getCameraGroupId()).orElse(null);
                            if (oldGroup != null && oldGroup.getId() != cameraGroupEntity.getId()) {
                                String indexSetting = oldGroup.getIndexSetting();
                                List<String> list = new LinkedList<>(Arrays.asList(indexSetting.split(", ")));
                                list.remove(cameraEntity.getCameraName());
                                String newIndexSetting = String.join(", ", list);
                                oldGroup.setIndexSetting(newIndexSetting);
                                cameraGroupRepository.save(oldGroup);
                            }
                        }
                        cameraEntity.setCameraGroupId(cameraGroupEntity.getId());
                        cameraEntity.setUpdatedBy(securityUser.getUuidId());
                        cameraEntity.setUpdatedTime(new Date().getTime());
                        cameraRepository.save(cameraEntity);
                        cameraEntities.removeIf(item -> item.getId().equals(cameraEntity.getId()));
                        for (CameraEntity cameraEntity1 : cameraEntities) {
                            cameraEntity1.setCameraGroupId(null);
                            cameraEntity1.setUpdatedBy(securityUser.getUuidId());
                            cameraEntity1.setUpdatedTime(new Date().getTime());
                            cameraRepository.save(cameraEntity1);
                        }
                    }
                }
            } else {
                for (CameraEntity cameraEntity : cameraEntities) {
                    cameraEntity.setCameraGroupId(null);
                    cameraEntity.setUpdatedBy(securityUser.getUuidId());
                    cameraEntity.setUpdatedTime(new Date().getTime());
                    cameraRepository.save(cameraEntity);
                }
            }
        }
    }

    public CameraGroupResponse saveOrUpdate(UUID cameraGroupId, CameraGroupDto cameraGroupDto, SecurityUser securityUser) throws ThingsboardException {
        CameraGroupEntity cameraGroupEntity;
        if (cameraGroupId == null) {
            if (cameraGroupRepository.existsByCameraGroupName(cameraGroupDto.getGroupName())) {
                throw new ThingsboardException("Tên nhóm đã tồn tại!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
            }
            cameraGroupEntity = new CameraGroupEntity();
            cameraGroupEntity.setId(UUID.randomUUID());
            cameraGroupEntity.setCreatedTime(new Date().getTime());
            cameraGroupEntity.setCreatedBy(securityUser.getId().getId());
        } else {
            cameraGroupEntity = cameraGroupRepository.findById(cameraGroupId).orElse(null);
            if (cameraGroupEntity != null) {
                if (!Objects.equals(cameraGroupEntity.getCameraGroupName(), cameraGroupDto.getGroupName()) && cameraGroupRepository.existsByCameraGroupName(cameraGroupDto.getGroupName())) {
                    throw new ThingsboardException("Tên nhóm đã tồn tại!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                }
                cameraGroupEntity.setUpdatedBy(securityUser.getId().getId());
                cameraGroupEntity.setUpdatedTime(new Date().getTime());
            }
        }
        if (cameraGroupEntity != null) {
            cameraGroupEntity.setCameraGroupName(cameraGroupDto.getGroupName());
            cameraGroupEntity.setTenantId(securityUser.getTenantId().getId());
            cameraGroupEntity.setIndexSetting(getIndexSetting(cameraGroupDto.getCameraIdList()));
            if (Boolean.TRUE.equals(cameraGroupDto.getIsDefault())) {
                getCameraGroupEntity(securityUser, cameraGroupEntity);
            }
            cameraGroupEntity.setDefault(cameraGroupDto.getIsDefault());

            cameraGroupEntity = cameraGroupRepository.save(cameraGroupEntity);
            refreshCameraAfterAddOrUpdateCamGroup(cameraGroupEntity, cameraGroupDto.getCameraIdList(), securityUser);
            String createName = null;
            long timeCreate = 0L;
            if (cameraGroupEntity.getCreatedBy() != null) {
                EndUserEntity endUserEntityCreated = endUserRepository.findByUserId(cameraGroupEntity.getCreatedBy()).orElse(null);
                if (endUserEntityCreated != null) {
                    createName = endUserEntityCreated.getName();
                    timeCreate = endUserEntityCreated.getCreatedTime();
                }
            }
            String updateName = null;
            if (cameraGroupEntity.getUpdatedBy() != null) {
                EndUserEntity endUserEntityUpdated = endUserRepository.findByUserId(cameraGroupEntity.getUpdatedBy()).orElse(null);
                if (endUserEntityUpdated != null) {
                    updateName = endUserEntityUpdated.getName();
                }

            }
            return CameraGroupResponse.builder().id(cameraGroupEntity.getId()).groupName(cameraGroupEntity.getCameraGroupName()).groupCameraNumber((long) cameraGroupDto.getCameraIdList().size()).cameraName(cameraGroupEntity.getIndexSetting()).createName(createName).createId(cameraGroupEntity.getCreatedBy()).createTime(timeCreate).isDefault(cameraGroupEntity.isDefault()).updateId(cameraGroupEntity.getUpdatedBy()).updateName(updateName).updateTime(cameraGroupEntity.getUpdatedTime()).build();
        }
        return null;
    }

    private String getIndexSetting(List<CameraIdDto> cameraIdList) {
        if (cameraIdList.isEmpty()) {
            return "";
        }
        List<String> cameraNameList = new ArrayList<>();
        for (CameraIdDto cameraId : cameraIdList) {
            cameraRepository.findById(cameraId.getCameraId()).ifPresent(cameraEntity -> cameraNameList.add(cameraEntity.getCameraName() + ":" + cameraEntity.getId()));
        }
        return String.join(", ", cameraNameList);
    }

    @Override
    public PageData<CameraGroupList> getPage(SecurityUser securityUser, Pageable pageable, String groupName) {

        Page<CameraGroupList> cameraGroupListPageData = cameraGroupRepository.findAllByName(securityUser.getTenantId().getId(), pageable, groupName).map(CameraGroupList::new);

        for (int i = 0; i < cameraGroupListPageData.getContent().size(); i++) {
            EndUserEntity userEntity = endUserRepository.findByUserId(cameraGroupListPageData.getContent().get(i).getCreateId()).orElse(null);
            if (userEntity != null) {
                cameraGroupListPageData.getContent().get(i).setCreateName(userEntity.getEmail());
            } else {
                UserEntity entity = tbUserRepository.findById(cameraGroupListPageData.getContent().get(i).getCreateId()).orElse(null);
                if (entity != null) {
                    cameraGroupListPageData.getContent().get(i).setCreateName(entity.getEmail());
                }
            }
            cameraGroupListPageData.getContent().get(i).setIsDefault(cameraGroupListPageData.getContent().get(i).getIsDefault());
            List<String> cameraNameList = new ArrayList<>();
            List<CameraEntity> cameraEntities = cameraRepository.findByCameraGroup(cameraGroupListPageData.getContent().get(i).getId());


            for (CameraEntity cameraEntity : cameraEntities) {
                cameraNameList.add(cameraEntity.getCameraName());
            }

            List<String> cameraNames = new ArrayList<>();
            CameraGroupEntity cameraGroupEntity = cameraGroupRepository.findByTenantIdAndId(securityUser.getTenantId().getId(), cameraGroupListPageData.getContent().get(i).getId());
            if (cameraGroupEntity != null) {
                String indexSettingString = cameraGroupEntity.getIndexSetting();
                if (indexSettingString.length() > 0) {
                    List<String> indexSettingList = Arrays.asList(indexSettingString.split(", "));
                    for (String indexSettingItem : indexSettingList) {
                        String cameraName = indexSettingItem.substring(0, indexSettingItem.lastIndexOf(":"));
                        cameraNames.add(cameraName);
                    }
                }

            }

            String cameraName = "";
            if (cameraNames.isEmpty()) {
                cameraName = "";
            } else {
                cameraName = String.join(", ", cameraNames);
            }

            int groupCameraNumber = cameraEntities.isEmpty() ? 0 : cameraEntities.size();
            cameraGroupListPageData.getContent().get(i).setGroupCameraNumber(groupCameraNumber);

            cameraGroupListPageData.getContent().get(i).setCameraName(cameraName);
        }
        return new PageData<>(cameraGroupListPageData.getContent(), cameraGroupListPageData.getTotalPages(), cameraGroupListPageData.getTotalElements(), cameraGroupListPageData.hasNext());
    }

    @Override
    @Transactional
    public void delete(SecurityUser securityUser, UUID id) {
        CameraGroupEntity cameraGroupEntity = cameraGroupRepository.findByIdAndTenantId(id, securityUser.getTenantId().getId()).orElse(null);
        if (cameraGroupEntity != null) {
            List<CameraEntity> cameraEntities = cameraRepository.findByCameraGroup1(id);
            if (!cameraEntities.isEmpty()) {
                for (CameraEntity cameraEntity : cameraEntities) {
                    cameraEntity.setCameraGroupId(null);
                    cameraEntity.setUpdatedBy(securityUser.getId().getId());
                    cameraEntity.setUpdatedTime(new Date().getTime());
                    cameraRepository.save(cameraEntity);
                }
            }
            cameraGroupRepository.deleteById(id);
        }
    }

    @Override
    public List<CameraGroupGetAllResponse> getAll(SecurityUser securityUser) {
        Sort sort = null;
        List<CameraGroupEntity> cameraGroupEntities = null;
        Boolean isExistByDefaul = cameraGroupRepository.existsByIsDefaultTrue();
        List<CameraGroupGetAllResponse> cameraGroupResponses = new ArrayList<>();

        sort = Sort.by("isDefault", "createdTime").descending();
        cameraGroupEntities = cameraGroupRepository.findAllByTenantId(sort, securityUser.getTenantId().getId());

        if (cameraGroupEntities != null) {
            for (CameraGroupEntity entity : cameraGroupEntities) {
                CameraGroupGetAllResponse response = new CameraGroupGetAllResponse();
                response.setCameraGroupId(entity.getId());
                response.setGroupName(entity.getCameraGroupName());
                response.setIsDefault(entity.isDefault());

                cameraGroupResponses.add(response);
            }
        }

        return cameraGroupResponses;
    }

    @Override
    @Transactional
    public List<CameraDetailDto> getAllByGroupID(UUID groupID, String cameraName, int noneGroup, SecurityUser
            securityUser) {
        List<CameraEntity> cameraEntityList = null;
        if (groupID == null && cameraName == null) {
            cameraEntityList = cameraRepository.findByTenantId(securityUser.getTenantId().getId());
        }
        if (groupID != null && cameraName == null) {
            cameraEntityList = cameraRepository.findByCameraGroupIdAndTenantId(groupID, securityUser.getTenantId().getId());
        }
        if (groupID != null && cameraName != null) {
            cameraEntityList = cameraRepository.findByCameraGroupIdAndTenantIdaAndCameraName(groupID, cameraName, securityUser.getTenantId().getId());
        }
        if (groupID == null && cameraName != null) {
            cameraEntityList = cameraRepository.findByTenantIAndCameraName(cameraName, securityUser.getTenantId().getId());
        }
        List<CameraDetailDto> response = cameraEntityList.stream().map(CameraDetailDto::new).collect(Collectors.toList());
        List<CameraDetailDto> detailDtoList = new ArrayList<>();
        for (CameraDetailDto dto : response) {
            if (noneGroup == 1) {
                if (dto.getGroupId() != null) {
                    cameraGroupRepository.findById(dto.getGroupId()).ifPresent(cameraGroupEntity -> dto.setGroupName(cameraGroupEntity.getCameraGroupName()));
                } else {
                    dto.setGroupName(null);
                }
                detailDtoList.add(dto);
            } else {
                if (dto.getGroupId() == null) {
                    detailDtoList.add(dto);
                }
            }
        }

        List<CameraDetailDto> detailDtoListSorted = new ArrayList<>();

        if (groupID != null) {
            CameraGroupEntity cameraGroupEntity = cameraGroupRepository.findByTenantIdAndId(securityUser.getTenantId().getId(), groupID);
            if (cameraGroupEntity != null) {
                String indexSettingString = cameraGroupEntity.getIndexSetting();
                List<String> indexSettingList = Arrays.asList(indexSettingString.split(", "));
                for (String indexSettingItem : indexSettingList) {
                    String cameraItemId = indexSettingItem.substring(indexSettingItem.lastIndexOf(":") + 1);
                    List<CameraDetailDto> cameraEntities = detailDtoList.stream().filter(item -> Objects.equals(item.getId().toString(), cameraItemId)).collect(Collectors.toList());
                    if (cameraEntities.size() == 1) {
                        detailDtoListSorted.add(cameraEntities.get(0));
                    }
                }
            }
        }


        return detailDtoListSorted;
    }

    @Transactional
    @Override
    public List<CameraGroupFilterDto> getListCameraGroupFilterDto(SecurityUser securityUser) {
        UUID tenantId = securityUser.getTenantId().getId();
        List<CameraGroupFilterDto> result = new ArrayList<>();
        Sort sort = Sort.by(Sort.Order.desc("isDefault"), Sort.Order.asc("cameraGroupName"));
        List<CameraGroupEntity> cameraGroupEntities = cameraGroupRepository.findAllByTenantId(sort, tenantId);
        if (!cameraGroupEntities.isEmpty()) {
            List<CameraEntity> inGroupCameras = new ArrayList<>();
            for (CameraGroupEntity entity : cameraGroupEntities) {
                CameraGroupFilterDto x = new CameraGroupFilterDto();
                x.setId(entity.getId());
                x.setTenantId(entity.getTenantId());
                x.setCameraGroupName(entity.getCameraGroupName());
                x.setDefault(entity.isDefault());

                List<CameraEntity> cameraEntities = cameraRepository.findByCameraGroupIdAndTenantId(entity.getId(), tenantId, Sort.by(Sort.Direction.ASC, "cameraName"));
                List<CameraDto> cameraDtos = cameraEntities.stream().map(CameraDto::new).collect(Collectors.toList());
                x.setListCameras(cameraDtos);
                result.add(x);

                inGroupCameras.addAll(cameraEntities);
            }

            // them nhom "chua co nhom"
            List<CameraEntity> allCameras = cameraRepository.findAllByTenantIdAndIsDelete(tenantId, false);
            List<CameraEntity> notInGroupCameras = allCameras.stream().filter(x -> !inGroupCameras.contains(x)).collect(Collectors.toList());
            if (!notInGroupCameras.isEmpty()) {
                List<CameraDto> notInGroupCamerasDto = notInGroupCameras.stream().map(CameraDto::new).collect(Collectors.toList());
                CameraGroupFilterDto c = new CameraGroupFilterDto();
                c.setId(UUID.randomUUID());
                c.setTenantId(tenantId);
                c.setCameraGroupName("Chưa có nhóm");
                c.setDefault(false);
                c.setListCameras(notInGroupCamerasDto);
                result.add(0, c);
            }
        }
        return result;
    }

    @Override
    public Boolean isExistedDefaultGroup() {
        return cameraGroupRepository.existsByIsDefaultTrue();
    }

    @Override
    public CameraGroupResponse setDefault(SecurityUser securityUser, UUID id, Boolean isDefault) {
        List<CameraGroupEntity> cameraGroupEntitiesExisted = cameraGroupRepository.findAll();
        CameraGroupEntity cameraGroupEntity = cameraGroupRepository.findByIdAndTenantId(id, securityUser.getTenantId().getId()).orElse(null);
        if (cameraGroupEntity != null) {
            checkIsDefault(securityUser, cameraGroupEntitiesExisted);
            if (Boolean.TRUE.equals(isDefault)) {
                cameraGroupEntity.setDefault(true);
            } else if (Boolean.FALSE.equals(isDefault)) {
                cameraGroupEntity.setDefault(false);
            }
            cameraGroupEntity.setUpdatedTime(new Date().getTime());
            cameraGroupEntity.setUpdatedBy(securityUser.getUuidId());
            cameraGroupRepository.save(cameraGroupEntity);
        }
        return null;
    }

    @Override
    public CameraGroupDto getById(SecurityUser securityUser, UUID id) {
        CameraGroupEntity cameraGroupEntity = cameraGroupRepository.findByTenantIdAndId(securityUser.getTenantId().getId(), id);
        if (cameraGroupEntity != null) {
            return new CameraGroupDto(cameraGroupEntity);
        }
        return null;
    }

    @Override
    public void removeIndex(CameraGroupDto cameraGroupDto) {
        CameraGroupEntity cameraGroupEntity = cameraGroupRepository.findByTenantIdAndId(cameraGroupDto.getTenantID(),
                cameraGroupDto.getId());
        if (cameraGroupEntity != null) {
            cameraGroupEntity.setIndexSetting(cameraGroupDto.getIndexSetting());
            cameraGroupRepository.save(cameraGroupEntity);
        }
    }

    private void checkIsDefault(SecurityUser securityUser, List<CameraGroupEntity> cameraGroupEntitiesExisted) {
        if (!cameraGroupEntitiesExisted.isEmpty()) {
            for (CameraGroupEntity existed : cameraGroupEntitiesExisted) {
                if (existed != null) {
                    existed.setDefault(false);
                    existed.setUpdatedBy(securityUser.getId().getId());
                    existed.setUpdatedTime(new Date().getTime());
                }
            }
            cameraGroupRepository.saveAll(cameraGroupEntitiesExisted);
        }
    }

    @NotNull
    public CameraGroupEntity getCameraGroupEntity(SecurityUser securityUser, CameraGroupEntity cameraGroupEntity) {
        List<CameraGroupEntity> cameraGroupEntitiesExisted = cameraGroupRepository.findAll();
        checkIsDefault(securityUser, cameraGroupEntitiesExisted);
        return cameraGroupEntity;
    }
}
