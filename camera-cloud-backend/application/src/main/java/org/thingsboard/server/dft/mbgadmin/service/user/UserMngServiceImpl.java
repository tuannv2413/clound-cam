package org.thingsboard.server.dft.mbgadmin.service.user;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.mbgadmin.dao.role.RoleDao;
import org.thingsboard.server.dft.mbgadmin.dao.user.UserMngDao;
import org.thingsboard.server.dft.mbgadmin.dao.user.UserRoleDao;
import org.thingsboard.server.dft.mbgadmin.dto.role.RoleDto;
import org.thingsboard.server.dft.mbgadmin.dto.user.UserLoginDto;
import org.thingsboard.server.dft.mbgadmin.dto.user.UserMngDto;

import java.util.List;
import java.util.UUID;

import static org.thingsboard.server.dao.service.Validator.validateId;

@Service
@Slf4j
public class UserMngServiceImpl implements UserMngService {

    private static final int DEFAULT_TOKEN_LENGTH = 30;
    private final UserMngDao userMngDao;
    private final RoleDao roleDao;
    private final UserRoleDao userRoleDao;

    @Autowired
    public UserMngServiceImpl(UserMngDao userMngDao, RoleDao roleDao, UserRoleDao userRoleDao) {
        this.userMngDao = userMngDao;
        this.roleDao = roleDao;
        this.userRoleDao = userRoleDao;
    }

    @Override
    public PageData<UserLoginDto> getPage(UUID roleId, Boolean active, Pageable pageable, String textSearch) {
        return userMngDao.getPage(roleId, active, pageable, textSearch);
    }

    @Override
    public boolean deleteById(UUID tenantId, UUID userId, UUID updateBy) {
        UserMngDto userMngDto = userMngDao.deleteById(tenantId, userId, updateBy);
        if (userMngDto != null) {
            userRoleDao.removeAllRoleByUserId(userMngDto.getTbUserId());
        }
        return userMngDto != null;
    }

    @Override
    public UserMngDto createOrUpdateUserMng(UserMngDto userDto, String password) throws ThingsboardException {
        UserMngDto userMngDto = userMngDao.createOrUpdateUserMng(userDto);
        return userMngDto;
    }

    @Override
    public UserMngDto upload(MultipartFile file, UUID id) {
        UserMngDto user = userMngDao.findById(id);
        try {
            byte[] image = Base64.encodeBase64(file.getBytes());
            String base64Result = new String(image);
            if (StringUtils.isNotEmpty(base64Result)) {
                user.setAvatar("data:image;base64," + base64Result);
            }
            user = userMngDao.createOrUpdateUserMng(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public User findUserByUserName(String userName) {
        return userMngDao.findUserByUserName(userName);
    }

    @Override
    public UserLoginDto findUserByUserId(UserId userId) {
        validateId(userId, "Incorrect userId " + userId);
        UserLoginDto loginDto = userMngDao.findUserByTbId(userId.getId());
        if (loginDto != null) {
            List<RoleDto> roles = roleDao.findRolesByUserId(loginDto.getTbUserId());
            List<String> perNames = roleDao.findPerNamesByUserId(roles);
            loginDto.setRoles(roles);
            loginDto.setPermissionNames(perNames);
        }
        return loginDto;
    }

    @Override
    public UserMngDto findByTbUserId(UUID id) {
        return userMngDao.findByTbUserId(id);
    }

    @Override
    public void addRole(UUID[] roles, UUID tbUserId) {

    }

    @Override
    public Integer findUsersCountByRoleId(UUID roleId) {
        return userMngDao.findUsersCountByRoleId(roleId);
    }

    @Override
    public UserLoginDto findUserByUserUUId(UserId userId) {
        validateId(userId, "Incorrect userId " + userId);
        UserLoginDto loginDto = userMngDao.findById(userId.getId());
        if (loginDto != null) {
            List<RoleDto> roles = roleDao.findRolesByUserId(loginDto.getTbUserId());
            List<String> perNames = roleDao.findPerNamesByUserId(roles);
            loginDto.setRoles(roles);
            loginDto.setPermissionNames(perNames);
        }
        return loginDto;
    }

    @Override
    public UserLoginDto findUserByPhone(String phone) {
        return userMngDao.findUserByPhone(phone);
    }

    @Override
    public List<UserLoginDto> findAllUserByPhone(String phone) {
        return userMngDao.findAllUserByPhone(phone);
    }

    @Override
    public boolean checkAdminSystem(UUID id) {
        return userMngDao.checkAdminSystem(id);
    }
}
