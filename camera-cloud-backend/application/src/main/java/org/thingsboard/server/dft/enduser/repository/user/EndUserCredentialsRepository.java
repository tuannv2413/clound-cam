package org.thingsboard.server.dft.enduser.repository.user;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dao.model.sql.UserCredentialsEntity;

import java.util.UUID;

@Repository
public interface EndUserCredentialsRepository extends JpaRepository<UserCredentialsEntity, UUID> {

    UserCredentialsEntity findByUserId(UUID userId);

    UserCredentialsEntity findByResetToken(String resetToken);
}
