package org.thingsboard.server.dft.enduser.service.camera;

import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.dft.enduser.dto.camera.CustomerCameraPermissionDto;
import org.thingsboard.server.dft.enduser.dto.camera.CustomerCameraPermissionSaveDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;

public interface CustomerCameraPermissionService {

    List<CustomerCameraPermissionDto> getAllCustomerCameraPermission(UUID boxId, UUID groupId, UUID clUserId, String cameraName, int noneGroup, UUID tenantId) throws ThingsboardException;

    List<CustomerCameraPermissionDto> saveCustomerCameraPermission(CustomerCameraPermissionSaveDto request ,UUID userId) throws ThingsboardException;

    boolean checkPtzPermission(SecurityUser securityUser, UUID cameraId);

    boolean checkHistoryPermission(SecurityUser securityUser, UUID cameraId);

    boolean checkViewLivePermission(SecurityUser securityUser, UUID cameraId);
}
