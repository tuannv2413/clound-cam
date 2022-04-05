package org.thingsboard.server.dft.mbgadmin.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dft.mbgadmin.dao.user.UserRoleDao;

import java.util.UUID;

@Service
public class UserRoleServiceImpl implements UserRoleService {
    private final UserRoleDao userRoleDao;

    @Autowired
    public UserRoleServiceImpl(UserRoleDao userRoleDao) {
        this.userRoleDao = userRoleDao;
    }


    @Override
    public void addUserRoles(UUID userId, UUID[] roleIds) {
        userRoleDao.addUserRoles(userId, roleIds);
    }

    @Override
    public void removeUserRoles(UUID userId, UUID[] roleIds) {
        userRoleDao.removeUserRoles(userId, roleIds);
    }

    @Override
    public void removeAllRoleByUserId(UUID userId) {
        userRoleDao.removeAllRoleByUserId(userId);
    }

    @Override
    public void updateTbUserRoles(UUID tbUserId, UUID[] roleIds) {
        userRoleDao.updateTbUserRoles(tbUserId, roleIds);
    }
}
