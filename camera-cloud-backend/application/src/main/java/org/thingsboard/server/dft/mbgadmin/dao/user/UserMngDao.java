package org.thingsboard.server.dft.mbgadmin.dao.user;

import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.mbgadmin.dto.user.UserLoginDto;
import org.thingsboard.server.dft.mbgadmin.dto.user.UserMngDto;
import org.thingsboard.server.dft.mbgadmin.entity.user.UserMngEntity;

import java.util.List;
import java.util.UUID;

public interface UserMngDao {
    PageData<UserLoginDto> getPage(UUID roleId, Boolean active, Pageable pageable, String textSearch);

    UserMngDto deleteById(UUID tenantId, UUID userId, UUID updateBy);

    UserMngDto createOrUpdateUserMng(UserMngDto userDto) throws ThingsboardException;

    User findUserByUserName(String userName);

    UserLoginDto findUserByTbId(UUID id);

    UserLoginDto findById(UUID id);

    UserMngDto findByTbUserId(UUID id);

    Integer findUsersCountByRoleId(UUID roleId);

    UserLoginDto findUserByPhone(String phone);

    List<UserLoginDto> findAllUserByPhone(String phone);

    boolean checkAdminSystem(UUID id);
}
