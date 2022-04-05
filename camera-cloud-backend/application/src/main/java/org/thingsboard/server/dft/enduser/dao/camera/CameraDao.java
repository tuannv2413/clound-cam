package org.thingsboard.server.dft.enduser.dao.camera;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dto.camera.AddCameraGroupDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDetailDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraEditDto;
import org.thingsboard.server.dft.enduser.dto.cameraGroup.CameraStreamGroup;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;

public interface CameraDao {

    PageData<CameraDto> getPageCamera(Pageable pageable, String searchText, UUID boxId, UUID tenantId);

    List<CameraStreamGroup> getAllCameraWithGroup(String searchText, SecurityUser securityUser) throws JsonProcessingException;

    CameraStreamGroup getCameraStreamByGroup(UUID groupId, String searchText, SecurityUser securityUser) throws JsonProcessingException;

    CameraEditDto save(CameraEditDto cameraEditDto, SecurityUser securityUser);

    CameraEditDto update(CameraEditDto cameraEditDto, SecurityUser securityUser);

    CameraEditDto changeBox(CameraEditDto cameraEditDto, SecurityUser securityUser);

    CameraEditDto getCameraEditById(UUID id);

    CameraEditDto getCameraEditByTenantIdAndTbDeviceId(UUID tenantId, UUID tbDeviceId);

    CameraDetailDto getCameraDetailDtoById(UUID id);

    void deleteById(UUID tenantId, UUID id);

    void deleteByBoxId(UUID tenantId, UUID boxId);

    boolean existsByTbDeviceIdAndTenantId(UUID tbDeviceId, UUID tenantId);

    boolean existsByTbDeviceIdAndTenantIdAndBoxId(UUID tbDeviceId, UUID tenantId, UUID boxId);

    boolean existsByTbDeviceIdAndTenantIdAndIdNot(UUID tbDeviceId, UUID tenantId, UUID id);

    boolean existsByCameraNameAndTenantId(String cameraName, UUID tenantId);

    boolean existsByCameraNameAndTenantIdAndIdNot(String cameraName, UUID tenantId, UUID id);

    void deleteFromGroup(UUID tenantId, UUID id);

    void addToGroup(UUID tenantId, UUID id, AddCameraGroupDto addCameraGroupDto);

    CameraDetailDto existByBoxIdAndIpv4(UUID boxId, String ipv4);
}
