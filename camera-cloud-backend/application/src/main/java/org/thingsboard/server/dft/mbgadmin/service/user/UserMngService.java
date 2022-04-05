package org.thingsboard.server.dft.mbgadmin.service.user;

import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.UserId;
import org.springframework.web.multipart.MultipartFile;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.mbgadmin.dto.user.UserLoginDto;
import org.thingsboard.server.dft.mbgadmin.dto.user.UserMngDto;

import java.util.List;
import java.util.UUID;

public interface UserMngService {
    PageData<UserLoginDto> getPage(UUID roleId, Boolean active, Pageable pageable, String textSearch);

    boolean deleteById(UUID tenantId, UUID userId, UUID updateBy);

    User findUserByUserName(String userName);

    UserLoginDto findUserByUserId(UserId userId);

    UserMngDto createOrUpdateUserMng(UserMngDto userDto, String password) throws ThingsboardException;

    UserMngDto upload(MultipartFile file, UUID id);

    UserMngDto findByTbUserId(UUID id);

    void addRole(UUID[] roles, UUID tbUserId);

    Integer findUsersCountByRoleId(UUID roleId);

    UserLoginDto findUserByUserUUId(UserId userId);

    UserLoginDto findUserByPhone(String phone);

    List<UserLoginDto> findAllUserByPhone(String phone);

    boolean checkAdminSystem(UUID id);
}
