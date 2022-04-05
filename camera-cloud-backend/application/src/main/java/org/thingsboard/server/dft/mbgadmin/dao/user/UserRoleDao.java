package org.thingsboard.server.dft.mbgadmin.dao.user;

import java.util.UUID;

public interface UserRoleDao {
    void addUserRoles(UUID userId, UUID[] roleIds);
    void removeUserRoles(UUID userId, UUID[] roleIds);
    void removeAllRoleByUserId(UUID userId);

    //remove all old roles and add new roles
    void updateTbUserRoles(UUID userId, UUID[] roleIds);
}
