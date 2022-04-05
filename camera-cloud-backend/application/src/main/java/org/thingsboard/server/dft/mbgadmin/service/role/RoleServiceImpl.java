package org.thingsboard.server.dft.mbgadmin.service.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.mbgadmin.dao.role.RoleDao;
import org.thingsboard.server.dft.mbgadmin.dto.role.RoleDto;
import org.thingsboard.server.dft.mbgadmin.entity.role.RoleEntity;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.*;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleDao roleDao;

    @Autowired
    public RoleServiceImpl(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    @Override
    public RoleDto createOrUpdate(RoleDto roleDto, SecurityUser securityUser) {
        roleDto.setCreatedTime(new Date().getTime());
        roleDto.setUpdatedTime(new Date().getTime());
        roleDto.setCreatedBy(securityUser.getUuidId());
        roleDto.setUpdatedBy(securityUser.getUuidId());
        return roleDao.createOrUpdate(roleDto, securityUser);
    }

    @Override
    public List<RoleDto> findRolesByUserId(UserId userId) {
        return roleDao.findRolesByUserId(userId.getId());
    }

    @Override
    public List<String> findPermissionByUserId(UserId userId) {
        List<RoleDto> roleDtos = findRolesByUserId(userId);
        List<String> perNames = new ArrayList<>();
        roleDtos.stream().map(r -> r.getPermissionEntities()).forEach(ps -> {
            ps.forEach(p -> perNames.add(p.getPermission()));
        });
        return perNames;
    }
    public PageData<RoleDto> getPage(Pageable pageable, String search, String sortProperty, String sortOrder) {
        return roleDao.getPage(pageable, search, sortProperty, sortOrder);
    }

    @Override
    public RoleDto getById(UUID id) {
        return roleDao.getById(id);
    }

    @Override
    public void deleteById(UUID id) {
        roleDao.deleteById(id);
    }

    @Override
    public Set<RoleDto> findAllById(UUID[] ids) {
        return roleDao.findAllById(ids);
    }

    @Override
    public RoleDto findRoleEntityByRoleName(String roleName) {
        return roleDao.findRoleEntityByRoleName(roleName);
    }

    @Override
    public boolean existsRoleEntityByRoleName(String roleName) {
        return roleDao.existsRoleEntityByRoleName(roleName);
    }

    @Override
    public List<RoleDto> findAll() {
        return roleDao.findAll();
    }
}
