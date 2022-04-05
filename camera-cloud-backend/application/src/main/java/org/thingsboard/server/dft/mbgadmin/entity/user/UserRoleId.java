package org.thingsboard.server.dft.mbgadmin.entity.user;

import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
public class UserRoleId implements Serializable {
    private UUID userId;

    private UUID roleId;

    public UserRoleId(UUID userId, UUID roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }
}
