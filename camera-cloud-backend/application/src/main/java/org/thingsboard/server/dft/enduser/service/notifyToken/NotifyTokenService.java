package org.thingsboard.server.dft.enduser.service.notifyToken;

import org.thingsboard.server.dft.enduser.dto.notifyToken.NotifyTokenDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;

public interface NotifyTokenService {
    NotifyTokenDto saveNotifyToken(NotifyTokenDto notifyTokenDto, SecurityUser securityUser);

    List<NotifyTokenDto> findAllByTbUserId(UUID tbUserId);
}
