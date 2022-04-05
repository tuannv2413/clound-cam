package org.thingsboard.server.dft.mbgadmin.dto.role;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.BaseInfoDto;
import org.thingsboard.server.dft.mbgadmin.entity.permission.PermissionEntity;
import org.thingsboard.server.dft.mbgadmin.entity.role.RoleEntity;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class RoleDto extends BaseInfoDto {
    private UUID id;
    private String roleName;
    private String roleType;
    private Integer usersCount;
    private String note;
    private Set<PermissionEntity> permissionEntities;
    public RoleDto(RoleEntity roleEntity) {
        super(roleEntity);
        this.id = roleEntity.getId();
        this.roleName = roleEntity.getRoleName();
        this.roleType = roleEntity.getRoleType();
        this.note = roleEntity.getNote();
        this.usersCount = roleEntity.getTbUserEntity().size();
        this.permissionEntities = roleEntity.getPermissionEntities();
    }
}
