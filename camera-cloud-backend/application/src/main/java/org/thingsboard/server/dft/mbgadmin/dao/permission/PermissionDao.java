package org.thingsboard.server.dft.mbgadmin.dao.permission;

import org.thingsboard.server.dft.mbgadmin.dto.permission.PermissionDto;
import org.thingsboard.server.dft.mbgadmin.dto.permission.PermissionGetDto;

import java.util.List;

public interface PermissionDao {
    List<PermissionGetDto> getAll();

    List<PermissionDto> findAll();


}
