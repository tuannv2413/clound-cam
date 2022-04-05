package org.thingsboard.server.dft.mbgadmin.dto.permission;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PermissionGetChildDto {
    private String name;
    private List<PermissionDto> permissions;
}
