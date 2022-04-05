package org.thingsboard.server.dft.enduser.dao.user;

import org.thingsboard.server.common.data.User;
import org.thingsboard.server.dao.Dao;
import org.thingsboard.server.dao.TenantEntityDao;
import org.thingsboard.server.dao.model.sql.UserEntity;

import java.util.UUID;

public interface UserDao extends Dao<User>, TenantEntityDao {

    UserEntity findByEndUserId(UUID tenantId, UUID endUserId);

    UserEntity findByEmail(String email);

    void save(User user);
}
