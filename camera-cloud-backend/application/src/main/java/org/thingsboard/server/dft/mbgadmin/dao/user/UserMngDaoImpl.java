package org.thingsboard.server.dft.mbgadmin.dao.user;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dao.DaoUtil;
import org.thingsboard.server.dao.model.sql.AuditLogEntity;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dao.sql.audit.AuditLogRepository;
import org.thingsboard.server.dft.mbgadmin.dto.role.RoleDto;
import org.thingsboard.server.dft.mbgadmin.dto.user.UserLoginDto;
import org.thingsboard.server.dft.mbgadmin.dto.user.UserMngDto;
import org.thingsboard.server.dft.mbgadmin.entity.role.RoleEntity;
import org.thingsboard.server.dft.mbgadmin.entity.user.UserMngEntity;
import org.thingsboard.server.dft.mbgadmin.repository.role.RoleRepository;
import org.thingsboard.server.dft.mbgadmin.repository.user.UserEntityRepository;
import org.thingsboard.server.dft.mbgadmin.repository.user.UserMngRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class UserMngDaoImpl implements UserMngDao {

    private final UserMngRepository userMngRepository;

    private final UserEntityRepository userEntityRepository;

    private final AuditLogRepository auditLogRepository;

    private final RoleRepository roleRepository;

    @Autowired
    public UserMngDaoImpl(UserMngRepository userRepository, UserEntityRepository userEntityRepository, AuditLogRepository auditLogRepository, RoleRepository roleRepository) {
        this.userMngRepository = userRepository;
        this.userEntityRepository = userEntityRepository;
        this.auditLogRepository = auditLogRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public PageData<UserLoginDto> getPage(UUID roleId, Boolean active, Pageable pageable, String textSearch) {
//        Page<UserMngDto> exampleDtoPage = userMngRepository.findAllByName(pageable, textSearch).map(UserMngDto::new);
//        Page<UserMngEntity> exampleDtoPage = userMngRepository.filterAll(pageable, textSearch, active);
        Page<UserMngEntity> exampleDtoPage = userMngRepository.findAllByName(pageable, textSearch, active, roleId);
        List<UserMngEntity> userMngEntitys = exampleDtoPage.getContent();
        List<UserLoginDto> userLoginDtos = new ArrayList<>();
        for (UserMngEntity item : userMngEntitys) {
            List<RoleDto> roles = findRolesByUserId(item.getTbUserId());
            List<String> perNames = findPerNamesByUserId(roles);

            UserLoginDto userLoginDto = new UserLoginDto(item, roles, perNames);
            userLoginDtos.add(userLoginDto);
        }

        return new PageData<>(userLoginDtos, exampleDtoPage.getTotalPages(), exampleDtoPage.getTotalElements(), exampleDtoPage.hasNext());
    }

    @Override
    public UserMngDto deleteById(UUID tenantId, UUID userId, UUID updateBy) {
        UserMngEntity userMngEntity = userMngRepository.findById(userId).orElse(null);
        if (userMngEntity == null || userMngEntity.isDelete()) {
            return null;
        }
        try {
            Page<AuditLogEntity> pageAuditLogEntity = auditLogRepository.findAuditLogsByTenantIdAndUserId(tenantId, userMngEntity.getTbUserId(), "", null, null, List.of(ActionType.LOGIN), PageRequest.of(0, 1));
            if (pageAuditLogEntity != null && pageAuditLogEntity.getTotalElements() > 0) {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("get audit log failed with exception: ", ex);
        }
        userMngEntity.setActive(false);
        userMngEntity.setDelete(true);
        userMngEntity.setUpdatedTime(System.currentTimeMillis());
        userMngEntity.setUpdatedBy(updateBy);
        userMngRepository.save(userMngEntity);
        return new UserMngDto(userMngEntity);
    }

    @Override
    public UserMngDto createOrUpdateUserMng(UserMngDto userMngDto) throws ThingsboardException {
//        List<UserMngEntity> lstUserMngEntity = userMngRepository.findByEmailAndPhone(userMngDto.getEmail(), userMngDto.getPhone());
//        if (lstUserMngEntity != null && !lstUserMngEntity.isEmpty()) {
//            return null;
//        }
        UserMngEntity userMngEntity = new UserMngEntity();
        if (userMngDto.getId() != null) {
            Optional<UserMngEntity> optionalUserEntity = userMngRepository.findById(userMngDto.getId());

            if (optionalUserEntity.isPresent()) {
                userMngEntity = optionalUserEntity.get();
                userMngEntity.setUpdatedTime(userMngDto.getUpdatedTime());
                userMngEntity.setUpdatedBy(userMngDto.getUpdatedBy());
            } else {
                userMngEntity.setId(userMngDto.getId());
                userMngEntity.setCreatedBy(userMngDto.getCreatedBy());
                userMngEntity.setCreatedTime(userMngDto.getCreatedTime());
            }
        } else {
            userMngEntity.setId(UUID.randomUUID());
            userMngEntity.setCreatedBy(userMngDto.getCreatedBy());
            userMngEntity.setCreatedTime(userMngDto.getCreatedTime());
        }
        if (userMngDto.getPhone() != null && userMngDto.getPhone().trim().length() > 0) {
            UserMngEntity existUserMngEntity = userMngRepository.findByPhone(userMngDto.getPhone().trim());
            if (existUserMngEntity != null && userMngDto.getId() != null && !existUserMngEntity.getId().equals(userMngDto.getId())) {
                throw new ThingsboardException("Số điện thoại đã tồn tại!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
            }
        }
        userMngEntity.setTenantId(userMngDto.getTenantId());
        userMngEntity.setName(userMngDto.getName());
        userMngEntity.setPhone(userMngDto.getPhone());
        userMngEntity.setEmail(userMngDto.getEmail());
        userMngEntity.setActive(userMngDto.isActive());
        userMngEntity.setType(userMngDto.getType());
        userMngEntity.setAvatar(userMngDto.getAvatar());
        userMngEntity.setOffice(userMngDto.getOffice());
        userMngEntity.setSearchText(userMngDto.getSearchText());
        userMngEntity.setActive(userMngDto.isActive());
        if (userMngDto.getTbUserId() != null) {
            userMngEntity.setTbUserId(userMngDto.getTbUserId());
        }

        userMngEntity = userMngRepository.save(userMngEntity);

        Optional<UserEntity> optionalUserEntity = userEntityRepository.findById(userMngEntity.getTbUserId());
        if (optionalUserEntity.isPresent()) {
            UserEntity userEntity = optionalUserEntity.get();
            userEntity.setFirstName(userMngEntity.getName());
            userEntity.setLastName(userMngEntity.getName());
            userEntity.setEmail(userMngEntity.getEmail());
            userEntity.setSearchText(userMngEntity.getName() + "; " + userMngEntity.getEmail() + "; " + userMngEntity.getPhone() + "; " + userMngEntity.getOffice());
            userEntityRepository.save(userEntity);
        }

        return new UserMngDto(userMngEntity);
    }

    @Override
    public UserLoginDto findById(UUID id) {
        Optional<UserMngEntity> optionalUserEntity = userMngRepository.findById(id);
        UserLoginDto userLoginDto = null;
        if (optionalUserEntity.isPresent()) {
            userLoginDto = new UserLoginDto(optionalUserEntity.get(), null, null);
        }
//        return optionalUserEntity.map(UserMngDto::new).orElse(null);
        return userLoginDto;
    }

    @Override
    public User findUserByUserName(String userName) {
        UserEntity userEntity = userMngRepository.findUserByUserName(userName);
        User user = null;
        if (userEntity != null) {
            user = DaoUtil.getData(userEntity);
        }
        return user;
    }

    @Override
    public UserLoginDto findUserByTbId(UUID id) {
        UserMngEntity userMng = userMngRepository.findByRelationUserId(id);
        UserLoginDto userLoginDto = null;
        if (userMng != null) {
            userLoginDto = new UserLoginDto(userMng, null, null);
        }
        return userLoginDto;
    }

    public UserMngDto findByTbUserId(UUID id) {
        UserMngEntity userMngEntity = userMngRepository.findByTbUserIdAndActiveAndDelete(id,true,false);
        if (userMngEntity == null) {
            //log.info("null");
            return null;
        }
        return new UserMngDto(userMngEntity);
    }

    @Override
    public Integer findUsersCountByRoleId(UUID roleId) {
        return userMngRepository.findUsersCountByRoleId(roleId);
    }

    @Override
    public UserLoginDto findUserByPhone(String phone) {
        UserMngEntity userMngEntity = userMngRepository.findByPhone(phone);
        if (userMngEntity == null) {
            return null;
        }
        return new UserLoginDto(userMngEntity, null, null);
    }

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

    public List<String> findPerNamesByUserId(@NotNull List<RoleDto> roleDtos) {
        List<String> perNames = new ArrayList<>();
        roleDtos.stream().map(RoleDto::getPermissionEntities).forEach(ps -> {
            ps.forEach(p -> {
                perNames.add(p.getPermission());
            });
        });
        return perNames;
    }

    @Override
    public List<UserLoginDto> findAllUserByPhone(String phone) {
        List<UserLoginDto> res = new ArrayList<>();
        List<UserMngEntity> userMngEntitys = userMngRepository.findAllByPhone(phone);
        for (UserMngEntity u : userMngEntitys) {
            res.add(new UserLoginDto(u, null, null));
        }
        return res;
    }

    @Override
    public boolean checkAdminSystem(UUID id) {
        List<UserMngEntity> mngEntities = userMngRepository.findByTbUserId(id);
        if (mngEntities.isEmpty())
            return true;
        return false;
    }
}
