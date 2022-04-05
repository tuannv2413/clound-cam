package org.thingsboard.server.dft.enduser.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dao.model.sql.UserEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TBUserRepository extends JpaRepository<UserEntity, UUID> {

    @Query("select u from UserEntity u " +
            " join EndUserEntity eu on eu.userId = u.id " +
            " where eu.id = :endUserId and eu.delete = false")
    UserEntity findByEndUserId(@Param("endUserId") UUID endUserId);

    UserEntity findByEmailIgnoreCase(String email);

    @Query("select u from UserEntity u " +
            " where u.email =:email and u.id <> :id")
    UserEntity findByEmailAndDifferentId(@Param("email") String email, @Param("id") UUID id);

    Optional<UserEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}
