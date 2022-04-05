package org.thingsboard.server.dft.mbgadmin.service.user;

import java.util.UUID;

public interface UserRoleService {
    void addUserRoles(UUID userId, UUID[] roleIds);
    void removeUserRoles(UUID userId, UUID[] roleIds);
    void removeAllRoleByUserId(UUID userId);

    //remove all old roles and add new roles
    void updateTbUserRoles(UUID userId, UUID[] roleIds);
}
