package org.thingsboard.server.dft.enduser.repository.notifyToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.enduser.entity.notifyToken.NotifyTokenEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotifyTokenRepository extends JpaRepository<NotifyTokenEntity, UUID> {

    NotifyTokenEntity findFirstByNotifyToken(String token);

    List<NotifyTokenEntity> findAllByTbUserId(UUID tbUserId);
}
