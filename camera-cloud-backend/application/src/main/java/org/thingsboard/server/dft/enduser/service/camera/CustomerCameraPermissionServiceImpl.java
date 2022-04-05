package org.thingsboard.server.dft.enduser.service.camera;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.dft.enduser.dao.camera.CustomerCameraPermissionDao;
import org.thingsboard.server.dft.enduser.dto.camera.CustomerCameraPermissionDto;
import org.thingsboard.server.dft.enduser.dto.camera.CustomerCameraPermissionSaveDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerCameraPermissionServiceImpl implements CustomerCameraPermissionService {

    private final CustomerCameraPermissionDao customerCameraPermissionDao;

    @Autowired
    public CustomerCameraPermissionServiceImpl(
            CustomerCameraPermissionDao customerCameraPermissionDao) {
        this.customerCameraPermissionDao = customerCameraPermissionDao;
    }

    @Override
    public List<CustomerCameraPermissionDto> getAllCustomerCameraPermission(UUID boxId, UUID groupId, UUID clUserId, String cameraName, int noneGroup, UUID tenantId) throws ThingsboardException {
        return customerCameraPermissionDao.getAllCustomerCameraPermissionV1(boxId, groupId, clUserId, cameraName, noneGroup, tenantId);
    }

    @Override
    public List<CustomerCameraPermissionDto> saveCustomerCameraPermission(
            CustomerCameraPermissionSaveDto request, UUID userId) throws ThingsboardException {
        return customerCameraPermissionDao.saveCustomerCameraPermission(request, userId);
    }

    // huydv thêm để check permssion các màn
    @Override
    public boolean checkPtzPermission(SecurityUser securityUser, UUID cameraId) {
        return customerCameraPermissionDao.checkPtzPermission(securityUser, cameraId);
    }

    @Override
    public boolean checkHistoryPermission(SecurityUser securityUser, UUID cameraId) {
        return customerCameraPermissionDao.checkHistoryPermission(securityUser, cameraId);
    }

    @Override
    public boolean checkViewLivePermission(SecurityUser securityUser, UUID cameraId) {
        return customerCameraPermissionDao.checkViewLivePermission(securityUser, cameraId);
    }
}
