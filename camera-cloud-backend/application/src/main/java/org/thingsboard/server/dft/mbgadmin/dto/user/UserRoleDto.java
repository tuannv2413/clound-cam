package org.thingsboard.server.dft.mbgadmin.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.thingsboard.server.dft.mbgadmin.entity.user.UserRoleEntity;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class UserRoleDto {
    private UUID userId;
    private UUID roleId;

    public UserRoleDto(UserRoleEntity userRoleEntity){
        this.userId = userRoleEntity.getUserId();
        this.roleId = userRoleEntity.getRoleId();
    }
}
