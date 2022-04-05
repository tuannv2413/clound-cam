package org.thingsboard.server.dft.mbgadmin.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.mbgadmin.entity.user.UserRoleEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UUID> {
    List<UserRoleEntity> getAllByUserId(UUID userId);

    void deleteByUserIdAndRoleId(UUID userId, UUID roleId);

    void deleteByUserId(UUID tbUserId);
}
