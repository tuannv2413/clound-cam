package org.thingsboard.server.dft.enduser.dao.cameraGroup;

import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDetailDto;
import org.thingsboard.server.dft.enduser.dto.cameraGroup.*;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;

public interface CameraGroupDao {

    CameraGroupResponse save(CameraGroupDto cameraGroupDto, SecurityUser securityUser) throws ThingsboardException;

    PageData<CameraGroupList> getPage(SecurityUser securityUser, Pageable pageable, String groupName);

    void delete(SecurityUser securityUser, UUID id);

    List<CameraGroupGetAllResponse> getAll(SecurityUser securityUser);

    List<CameraDetailDto> getAllByGroupID(UUID groupID, String cameraName, int noneGroup, SecurityUser securityUser);

    List<CameraGroupFilterDto> getListCameraGroupFilterDto(SecurityUser securityUser);

    Boolean isExistedDefaultGroup();

    CameraGroupResponse setDefault(SecurityUser securityUser, UUID id, Boolean isDefault);

    CameraGroupDto getById(SecurityUser securityUser, UUID id);

    void removeIndex(CameraGroupDto cameraGroupDto);
}
