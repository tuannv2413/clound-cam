package org.thingsboard.server.dft.mbgadmin.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.dao.user.UserDao;

@Service
@Slf4j
public class TBUserServiceImpl implements TBUserService  {
    private final UserDao userDao;

    public TBUserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User changeUserEmail(User user) {
        log.trace("Executing saveUser [{}]", user);
        user.setEmail(user.getEmail().toLowerCase());
        return userDao.save(user.getTenantId(), user);
    }
}
