package org.thingsboard.server.dft.mbgadmin.service.role;

import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.dft.mbgadmin.dto.role.RoleDto;
import org.thingsboard.server.service.security.model.SecurityUser;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.page.PageData;
import java.util.UUID;

import java.util.Set;

public interface RoleService {
    RoleDto createOrUpdate(RoleDto roleDto, SecurityUser securityUser);

    PageData<RoleDto> getPage(Pageable pageable, String search, String sortProperty, String sortOrder);

    RoleDto getById(UUID id);

    void deleteById(UUID id);

    List<RoleDto> findRolesByUserId(UserId userId);

    List<String> findPermissionByUserId(UserId userId);

    Set<RoleDto> findAllById(UUID[] ids);

    RoleDto findRoleEntityByRoleName(String roleName);

    boolean existsRoleEntityByRoleName(String roleName);

    List<RoleDto> findAll();
}
