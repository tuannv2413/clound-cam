package org.thingsboard.server.dft.mbgadmin.service.user;

import org.thingsboard.server.common.data.User;

public interface TBUserService {
    User changeUserEmail(User user);
}
