package org.thingsboard.server.dft.enduser.service.cameraGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dao.attributes.AttributesDao;
import org.thingsboard.server.dft.enduser.dao.cameraGroup.CameraGroupDao;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDetailDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDto;
import org.thingsboard.server.dft.enduser.dto.cameraGroup.*;
import org.thingsboard.server.dft.util.constant.CameraInfoKeyConstant;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class CameraGroupServiceImpl implements CameraGroupService {

    private final CameraGroupDao cameraGroupDao;
    private final AttributesDao attributesDao;

    @Autowired
    public CameraGroupServiceImpl(CameraGroupDao cameraGroupDao, AttributesDao attributesDao) {
        this.cameraGroupDao = cameraGroupDao;
        this.attributesDao = attributesDao;
    }

    @Override
    public CameraGroupResponse save(CameraGroupDto cameraGroupDto, SecurityUser securityUser) throws ThingsboardException {
        return cameraGroupDao.save(cameraGroupDto, securityUser);
    }

    @Override
    public CameraGroupResponse setDefault(SecurityUser securityUser, UUID id, Boolean isDefault) {
        return cameraGroupDao.setDefault(securityUser, id, isDefault);
    }

    @Override
    public PageData<CameraGroupList> getPage(SecurityUser securityUser, Pageable pageable, String groupName) {
        return cameraGroupDao.getPage(securityUser, pageable, groupName);
    }

    @Override
    public void delete(SecurityUser securityUser, UUID id) {
        cameraGroupDao.delete(securityUser, id);
    }

    @Override
    public List<CameraGroupGetAllResponse> getAll(SecurityUser securityUser) {
        return cameraGroupDao.getAll(securityUser);
    }

    @Override
    public List<CameraDetailDto> getAllByGroupID(UUID groupID, String cameraName, int noneGroup, SecurityUser securityUser) {
        return cameraGroupDao.getAllByGroupID(groupID, cameraName, noneGroup, securityUser);
    }

    @Override
    public List<CameraGroupFilterDto> getListCameraGroupFilterDto(SecurityUser securityUser) throws ExecutionException, InterruptedException {
        List<CameraGroupFilterDto> result = cameraGroupDao.getListCameraGroupFilterDto(securityUser);
        for (CameraGroupFilterDto g : result) {
            for (CameraDto c : g.getListCameras()) {
                AttributeKvEntry resolutionAttribute =
                        findAttribute(securityUser.getTenantId(), new DeviceId(c.getTbDeviceId()),
                                CameraInfoKeyConstant.RESOLUTION, DataConstants.CLIENT_SCOPE);
                if (resolutionAttribute != null) {
                    c.setResolution(resolutionAttribute.getValueAsString());
                }
            };
        }
        return result;
    }

    @Override
    public Boolean isExistedDefaultGroup() {
        return cameraGroupDao.isExistedDefaultGroup();
    }

    @Override
    public void deleteCameraInIndexSetting(SecurityUser securityUser, UUID cameraId, UUID groupCameraId) throws ThingsboardException {
        CameraGroupDto cameraGroupDto = cameraGroupDao.getById(securityUser, groupCameraId);
        String indexSetting = cameraGroupDto.getIndexSetting();
        List<String> deviceIndexs = List.of(indexSetting.split(", "));
        deviceIndexs = deviceIndexs.stream().filter(deviceIndex -> !deviceIndex.contains(cameraId.toString()))
                .collect(Collectors.toList());
        indexSetting = deviceIndexs.stream().map(n -> String.valueOf(n))
                .collect(Collectors.joining(", "));
        cameraGroupDto.setIndexSetting(indexSetting);
        cameraGroupDao.removeIndex(cameraGroupDto);
    }

    private AttributeKvEntry findAttribute(TenantId tenantId, DeviceId deviceId, String key, String scope) throws ExecutionException, InterruptedException {
        Optional<AttributeKvEntry> optionalAttributeKvEntry =
                attributesDao.find(tenantId, deviceId, scope, key).get();
        if (optionalAttributeKvEntry.isEmpty()) {
            return null;
        }
        return optionalAttributeKvEntry.orElse(null);
    }
}
