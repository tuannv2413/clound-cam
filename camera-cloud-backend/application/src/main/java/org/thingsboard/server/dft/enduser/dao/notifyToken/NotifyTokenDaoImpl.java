package org.thingsboard.server.dft.enduser.dao.notifyToken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thingsboard.server.dft.enduser.dto.notifyToken.NotifyTokenDto;
import org.thingsboard.server.dft.enduser.entity.notifyToken.NotifyTokenEntity;
import org.thingsboard.server.dft.enduser.repository.notifyToken.NotifyTokenRepository;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class NotifyTokenDaoImpl implements NotifyTokenDao {

    private final NotifyTokenRepository notifyTokenRepository;

    @Autowired
    public NotifyTokenDaoImpl(NotifyTokenRepository notifyTokenRepository) {
        this.notifyTokenRepository = notifyTokenRepository;
    }

    @Override
    public NotifyTokenDto saveNotifyToken(NotifyTokenDto notifyTokenDto, SecurityUser securityUser) {
        NotifyTokenDto result;
        NotifyTokenEntity newNtEntity;
        NotifyTokenEntity currentNtEntity;
        NotifyTokenEntity savedNtEntity;
        currentNtEntity = notifyTokenRepository.findFirstByNotifyToken(notifyTokenDto.getNotifyToken());
        if (currentNtEntity == null) {
            newNtEntity = new NotifyTokenEntity();
            newNtEntity.setId(UUID.randomUUID());
            newNtEntity.setTbUserId(securityUser.getUuidId());
            newNtEntity.setNotifyToken(notifyTokenDto.getNotifyToken());
            newNtEntity.setCreatedTime(System.currentTimeMillis());
            newNtEntity.setCreatedby(securityUser.getUuidId());
            savedNtEntity = notifyTokenRepository.save(newNtEntity);
            result = toNotifyTokenDto(savedNtEntity);
        } else {
            if (currentNtEntity.getTbUserId().equals(securityUser.getUuidId())) {
                result = toNotifyTokenDto(currentNtEntity);
            } else {
                // update
                currentNtEntity.setTbUserId(securityUser.getUuidId());
                savedNtEntity = notifyTokenRepository.save(currentNtEntity);
                result = toNotifyTokenDto(savedNtEntity);
            }
        }
        return result;
    }

    @Override
    public List<NotifyTokenDto> findAllByTbUserId(UUID tbUserId) {
        List<NotifyTokenEntity> notifyTokenEntities = notifyTokenRepository.findAllByTbUserId(tbUserId);
        return notifyTokenEntities.stream().map(this::toNotifyTokenDto).collect(Collectors.toList());
    }

    private NotifyTokenDto toNotifyTokenDto(NotifyTokenEntity entity) {
        NotifyTokenDto result = new NotifyTokenDto();
        result.setId(entity.getId());
        result.setTbUserId(entity.getTbUserId());
        result.setNotifyToken(entity.getNotifyToken());
        result.setCreatedTime(entity.getCreatedTime());
        result.setCreatedby(entity.getCreatedby());

        return result;
    }
}
