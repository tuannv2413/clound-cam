package org.thingsboard.server.dft.enduser.dao.notifyToken;

import org.thingsboard.server.dft.enduser.dto.notifyToken.NotifyTokenDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;

public interface NotifyTokenDao {
    NotifyTokenDto saveNotifyToken(NotifyTokenDto notifyTokenDto, SecurityUser securityUser);

    List<NotifyTokenDto> findAllByTbUserId(UUID tbUserId);
}
