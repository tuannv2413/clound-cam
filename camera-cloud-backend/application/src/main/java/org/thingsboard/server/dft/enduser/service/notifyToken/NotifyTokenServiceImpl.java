package org.thingsboard.server.dft.enduser.service.notifyToken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dft.enduser.dao.notifyToken.NotifyTokenDao;
import org.thingsboard.server.dft.enduser.dto.notifyToken.NotifyTokenDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;

@Service
public class NotifyTokenServiceImpl implements NotifyTokenService {

    private final NotifyTokenDao notifyTokenDao;

    @Autowired
    public NotifyTokenServiceImpl(NotifyTokenDao notifyTokenDao) {
        this.notifyTokenDao = notifyTokenDao;
    }

    @Override
    public NotifyTokenDto saveNotifyToken(NotifyTokenDto notifyTokenDto, SecurityUser securityUser) {
        return notifyTokenDao.saveNotifyToken(notifyTokenDto, securityUser);
    }

    @Override
    public List<NotifyTokenDto> findAllByTbUserId(UUID tbUserId) {
        return notifyTokenDao.findAllByTbUserId(tbUserId);
    }
}
