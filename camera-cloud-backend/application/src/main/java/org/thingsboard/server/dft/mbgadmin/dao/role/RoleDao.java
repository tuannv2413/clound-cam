package org.thingsboard.server.dft.mbgadmin.dao.role;

import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.mbgadmin.dto.role.RoleDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RoleDao {
    RoleDto createOrUpdate(RoleDto roleDto, SecurityUser securityUser);

    List<RoleDto> findRolesByUserId(UUID id);

    PageData<RoleDto> getPage(Pageable pageable, String search, String sortProperty, String sortOrder);

    RoleDto getById(UUID id);

    void deleteById(UUID id);

    List<String> findPerNamesByUserId(List<RoleDto> roleDtos);
    Set<RoleDto> findAllById(UUID[] ids);

    RoleDto findRoleEntityByRoleName(String roleName);

    boolean existsRoleEntityByRoleName(String roleName);

    List<RoleDto> findAll();
}
