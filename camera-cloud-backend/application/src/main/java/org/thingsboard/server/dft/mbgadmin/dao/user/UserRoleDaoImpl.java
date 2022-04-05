package org.thingsboard.server.dft.mbgadmin.dao.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dao.sql.user.UserRepository;
import org.thingsboard.server.dft.mbgadmin.entity.user.UserRoleEntity;
import org.thingsboard.server.dft.mbgadmin.repository.user.UserRoleRepository;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserRoleDaoImpl implements UserRoleDao {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Autowired
    public UserRoleDaoImpl(UserRepository userRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }


    @Override
    public void addUserRoles(UUID userId, UUID[] roleIds) {
        Optional<UserEntity> optionalUserEntity = userRepository.findById(userId);
        if (optionalUserEntity.isPresent()) {
            for (UUID uuid : roleIds) {
                if (uuid == null) {
                    continue;
                }
                try {
                    UserRoleEntity userRoleEntity = new UserRoleEntity();
                    userRoleEntity.setUserId(userId);
                    userRoleEntity.setRoleId(uuid);
                    userRoleRepository.save(userRoleEntity);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void removeUserRoles(UUID userId, UUID[] roleIds) {
        Optional<UserEntity> optionalUserEntity = userRepository.findById(userId);
        if (optionalUserEntity.isPresent()) {
            for (UUID uuid : roleIds) {
//                UserRoleEntity userRoleEntity = new UserRoleEntity();
//                userRoleEntity.setUserId(userId);
//                userRoleEntity.setRoleId(uuid);
                userRoleRepository.deleteByUserIdAndRoleId(userId, uuid);
            }
        }
    }

    @Override
    @Transactional
    public void removeAllRoleByUserId(UUID userId) {
        userRoleRepository.deleteByUserId(userId);
    }

    @Override
    @Transactional
    public void updateTbUserRoles(UUID tbUserId, UUID[] roleIds) {
        Optional<UserEntity> optionalUserEntity = userRepository.findById(tbUserId);
        if (optionalUserEntity.isPresent()) {
            userRoleRepository.deleteByUserId(tbUserId);
            for (UUID uuid : roleIds) {
                if (uuid == null) {
                    continue;
                }
                try {
                    UserRoleEntity userRoleEntity = new UserRoleEntity();
                    userRoleEntity.setUserId(tbUserId);
                    userRoleEntity.setRoleId(uuid);
                    userRoleRepository.save(userRoleEntity);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
