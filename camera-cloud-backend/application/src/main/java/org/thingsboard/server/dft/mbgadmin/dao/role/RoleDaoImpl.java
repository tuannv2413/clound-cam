package org.thingsboard.server.dft.mbgadmin.dao.role;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.mbgadmin.dto.role.RoleDto;
import org.thingsboard.server.dft.mbgadmin.entity.role.RoleEntity;
import org.thingsboard.server.dft.mbgadmin.repository.role.RoleRepository;
import org.thingsboard.server.dft.mbgadmin.repository.user.UserMngRepository;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RoleDaoImpl implements RoleDao {
    private final RoleRepository roleRepository;
    private final UserMngRepository userMngRepository;

    @Autowired
    public RoleDaoImpl(RoleRepository roleRepository, UserMngRepository userMngRepository) {
        this.roleRepository = roleRepository;
        this.userMngRepository = userMngRepository;
    }

    @Override
    @Transactional
    public RoleDto createOrUpdate(RoleDto roleDto, SecurityUser securityUser) {
        RoleEntity roleEntity = new RoleEntity();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        if (roleDto.getId() != null) {
            Optional<RoleEntity> optionalRoleEntity = roleRepository.findById(roleDto.getId());
            if (optionalRoleEntity.isPresent()) {
                roleEntity = optionalRoleEntity.get();
                roleEntity.setUpdatedTime(timestamp.getTime());
                roleEntity.setUpdatedBy(securityUser.getUuidId());
                roleRepository.deleteById(roleDto.getId());
                roleRepository.deletePermissionRoles(roleDto.getId());
            } else {
                roleEntity.setId(roleDto.getId());
                roleEntity.setCreatedBy(securityUser.getUuidId());
                roleEntity.setCreatedTime(timestamp.getTime());
            }
        } else {
            roleEntity.setId(UUID.randomUUID());
            roleEntity.setCreatedBy(securityUser.getUuidId());
            roleEntity.setCreatedTime(timestamp.getTime());
            roleEntity.setUpdatedBy(securityUser.getUuidId());
            roleEntity.setUpdatedTime(timestamp.getTime());
        }
        roleEntity.setRoleName(roleDto.getRoleName());
        roleEntity.setPermissionEntities(roleDto.getPermissionEntities());
        roleEntity.setNote(roleDto.getNote());
        roleEntity.setRoleType("WEB_ADMIN");
        roleEntity = roleRepository.save(roleEntity);
        return new RoleDto(roleEntity);
    }

    @Override
    @Transactional
    public List<RoleDto> findRolesByUserId(UUID id) {
        List<RoleEntity> roles = roleRepository.findRolesByUser(id);
        List<RoleDto> roleDtos = new ArrayList<>();
        if (!roles.isEmpty()) {
            for (RoleEntity role : roles) {
               RoleDto roleDto = new RoleDto(role);
               roleDto.setUsersCount(userMngRepository.findUsersCountByRoleId(role.getId()));
               roleDtos.add(roleDto);
            }
        }
        return roleDtos;
    }
    public PageData<RoleDto> getPage(Pageable pageable, String search, String sortProperty, String sortOrder) {
        Page<RoleDto> roleDtoPage;
        if (sortProperty.equals("usersCount")) {
            if(sortOrder.equals("desc")){
                roleDtoPage = roleRepository.findAllByNameAndNoteOrderDESC(pageable, search).map(RoleDto::new);
            }else{
                roleDtoPage = roleRepository.findAllByNameAndNoteOrderASC(pageable, search).map(RoleDto::new);
            }
        } else {
            roleDtoPage = roleRepository.findAllByNameAndNote(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(sortOrder.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortProperty)), search).map(RoleDto::new);
        }
        return new PageData<>(roleDtoPage.getContent(), roleDtoPage.getTotalPages(),
                roleDtoPage.getTotalElements(), roleDtoPage.hasNext());
    }

    @Override
    public RoleDto getById(UUID id) {
        RoleEntity roleEntity;
        Optional<RoleEntity> optionalRoleEntity = roleRepository.findById(id);
        if (optionalRoleEntity.isPresent()) {
            roleEntity = optionalRoleEntity.get();
            RoleDto roleDto = new RoleDto(roleEntity);
            roleDto.setUsersCount(userMngRepository.findUsersCountByRoleId(roleDto.getId()));
            return roleDto;
        } else {
            return null;
        }
    }

    @Override
    public void deleteById(UUID id) {
        roleRepository.deleteById(id);
    }

    @Override
    public List<String> findPerNamesByUserId(@NotNull List<RoleDto>  roleDtos) {
        List<String> perNames = new ArrayList<>();
        roleDtos.stream().map(r -> r.getPermissionEntities()).forEach(ps -> {
            ps.forEach(p -> perNames.add(p.getPermission()));
        });
        return perNames;
    }
    
    public Set<RoleDto> findAllById(UUID[] ids) {
        return roleRepository.findAllById(Arrays.asList(ids)).stream().map(RoleDto::new).collect(Collectors.toSet());
    }

    @Override
    public RoleDto findRoleEntityByRoleName(String roleName) {
        RoleEntity roleEntity = roleRepository.findRoleEntityByRoleName(roleName);
        RoleDto roleDto = new RoleDto(roleEntity);
        return roleDto;
    }

    @Override
    public boolean existsRoleEntityByRoleName(String roleName) {
        return roleRepository.existsRoleEntityByRoleName(roleName);
    }

    @Override
    public List<RoleDto> findAll() {
        List<RoleEntity> roles = roleRepository.findAll(Sort.by(Sort.Direction.DESC, "createdTime"));
        List<RoleDto> roleDtoList = new ArrayList<>();
        if (!roles.isEmpty()) {
            for (RoleEntity role : roles) {
                RoleDto roleDto = new RoleDto(role);
                roleDto.setUsersCount(userMngRepository.findUsersCountByRoleId(role.getId()));
                roleDtoList.add(roleDto);
            }
        }
        return roleDtoList;
    }
}
