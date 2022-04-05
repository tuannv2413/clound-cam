package org.thingsboard.server.dft.mbgadmin.dto.permission;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.mbgadmin.entity.permission.PermissionEntity;

import java.util.UUID;

@Data
@NoArgsConstructor
public class PermissionDto {
    private UUID id;
    private String permission;
    @JsonIgnore
    private Integer order;
    public PermissionDto(PermissionEntity permissionEntity) {
        this.id = permissionEntity.getId();
        this.permission = permissionEntity.getPermission();
    }
}
