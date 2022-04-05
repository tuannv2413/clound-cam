package org.thingsboard.server.dft.enduser.dao.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dao.sql.JpaAbstractSearchTextDao;
import org.thingsboard.server.dft.enduser.repository.user.TBUserRepository;
import org.thingsboard.server.dft.mbgadmin.repository.user.UserEntityRepository;

import java.util.UUID;

@Component
public class UserDaoImpl extends JpaAbstractSearchTextDao<UserEntity, User> implements UserDao {

    @Autowired
    private TBUserRepository userRepository;

    @Override
    public UserEntity findByEndUserId(UUID tenantId, UUID endUserId) {
        return userRepository.findByEndUserId(endUserId);
    }

    @Override
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);

    }

    public void save(User user) {
        UserEntity userEntity = new UserEntity(user);
        userEntity.setSearchText(user.getSearchText());
        userRepository.save(userEntity);
    }

    @Override
    public Long countByTenantId(TenantId tenantId) {
        return null;
    }

    @Override
    protected Class<UserEntity> getEntityClass() {
        return null;
    }

    @Override
    protected CrudRepository<UserEntity, UUID> getCrudRepository() {
        return null;
    }
}
