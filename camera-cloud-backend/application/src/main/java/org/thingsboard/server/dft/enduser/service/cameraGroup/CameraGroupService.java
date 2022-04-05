package org.thingsboard.server.dft.enduser.service.cameraGroup;

import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDetailDto;
import org.thingsboard.server.dft.enduser.dto.cameraGroup.*;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface CameraGroupService {

    CameraGroupResponse save(CameraGroupDto cameraGroupDto, SecurityUser securityUser) throws ThingsboardException;

    CameraGroupResponse setDefault(SecurityUser securityUser, UUID id, Boolean isDefault);

    PageData<CameraGroupList> getPage(SecurityUser securityUser,Pageable pageable, String groupName);

    void delete(SecurityUser securityUser, UUID id);

    List<CameraGroupGetAllResponse> getAll(SecurityUser securityUser);

    List<CameraDetailDto> getAllByGroupID(UUID groupID,String cameraName, int noneGroup,SecurityUser securityUser);

    List<CameraGroupFilterDto> getListCameraGroupFilterDto(SecurityUser securityUser) throws ExecutionException, InterruptedException;

    Boolean isExistedDefaultGroup();

    void deleteCameraInIndexSetting(SecurityUser securityUser, UUID cameraId, UUID groupCameraId) throws ThingsboardException;
}
