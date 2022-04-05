package org.thingsboard.server.dft.mbgadmin.service.permission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dft.mbgadmin.dao.permission.PermissionDao;
import org.thingsboard.server.dft.mbgadmin.dto.permission.PermissionDto;
import org.thingsboard.server.dft.mbgadmin.dto.permission.PermissionGetDto;

import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService  {
    private final PermissionDao permissionDao;

    @Autowired
    public PermissionServiceImpl(PermissionDao permissionDao) {
        this.permissionDao = permissionDao;
    }

    @Override
    public List<PermissionGetDto> getAll() {
        return permissionDao.getAll();
    }

    @Override
    public List<PermissionDto> findAll() {
        return permissionDao.findAll();
    }
}
