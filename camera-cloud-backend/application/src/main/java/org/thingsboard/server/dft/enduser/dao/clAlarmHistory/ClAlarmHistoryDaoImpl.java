package org.thingsboard.server.dft.enduser.dao.clAlarmHistory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dto.clAlarmHistory.ClAlarmHistoryDto;
import org.thingsboard.server.dft.enduser.dto.clAlarmHistory.ClAlarmHistoryInfoDto;
import org.thingsboard.server.dft.enduser.entity.box.BoxEntity;
import org.thingsboard.server.dft.enduser.entity.camera.CameraEntity;
import org.thingsboard.server.dft.enduser.entity.clAlarmHistory.ClAlarmHistoryEntity;
import org.thingsboard.server.dft.enduser.repository.box.BoxRepository;
import org.thingsboard.server.dft.enduser.repository.camera.CameraRepository;
import org.thingsboard.server.dft.enduser.repository.clALarmHistory.ClAlarmHistoryRepository;
import org.thingsboard.server.dft.util.constant.ClDeviceConstant;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class ClAlarmHistoryDaoImpl implements ClAlarmHistoryDao {

    private final ClAlarmHistoryRepository clAlarmHistoryRepository;
    private final CameraRepository cameraRepository;
    private final BoxRepository boxRepository;

    @Autowired
    public ClAlarmHistoryDaoImpl(ClAlarmHistoryRepository clAlarmHistoryRepository, CameraRepository cameraRepository, BoxRepository boxRepository) {
        this.clAlarmHistoryRepository = clAlarmHistoryRepository;
        this.cameraRepository = cameraRepository;
        this.boxRepository = boxRepository;
    }

    @Override
    public ClAlarmHistoryDto save(ClAlarmHistoryDto clAlarmHistoryDto) throws ThingsboardException {
        ClAlarmHistoryDto result;
        ClAlarmHistoryEntity currentClAlarmHistoryEntity;
        ClAlarmHistoryEntity newClAlarmHistoryEntity;
        ClAlarmHistoryEntity savedClAlarmHistoryEntity;

        if (clAlarmHistoryDto.getId() == null) {
            clAlarmHistoryDto.setId(UUID.randomUUID());
            newClAlarmHistoryEntity = toClAlarmHistoryEntity(clAlarmHistoryDto);
            savedClAlarmHistoryEntity = clAlarmHistoryRepository.save(newClAlarmHistoryEntity);
            result = toClAlarmHistoryDto(savedClAlarmHistoryEntity);
        } else {
            currentClAlarmHistoryEntity = clAlarmHistoryRepository.findById(clAlarmHistoryDto.getId()).orElse(null);
            if (currentClAlarmHistoryEntity == null) {
                throw new ThingsboardException("Requested item wasn't found!", ThingsboardErrorCode.ITEM_NOT_FOUND);
            }
            currentClAlarmHistoryEntity.setTbAlarmId(clAlarmHistoryDto.getTbAlarmId());
            currentClAlarmHistoryEntity.setTbDeviceId(clAlarmHistoryDto.getTbDeviceId());
            currentClAlarmHistoryEntity.setDeviceType(clAlarmHistoryDto.getDeviceType());
            currentClAlarmHistoryEntity.setType(clAlarmHistoryDto.getType());
            currentClAlarmHistoryEntity.setViewed(clAlarmHistoryDto.isViewed());
            savedClAlarmHistoryEntity = clAlarmHistoryRepository.save(currentClAlarmHistoryEntity);
            result = toClAlarmHistoryDto(savedClAlarmHistoryEntity);
        }
        return result;
    }

    @Transactional
    @Override
    public PageData<ClAlarmHistoryInfoDto> getAllWithPaging(
            Pageable pageable,
            String textSearch,
            String type,
            UUID boxTbDeviceId,
            Boolean viewed,
            Long startTs,
            Long endTs,
            UUID tenantId) {
        Page<ClAlarmHistoryInfoDto> clAlarmHistoryInfoDtoPage;
        if (type == null && boxTbDeviceId == null){
            clAlarmHistoryInfoDtoPage = clAlarmHistoryRepository
                    .findAllWithPaging(tenantId, viewed, startTs, endTs, pageable)
                    .map(this::toClAlarmHistoryInfoDto);
        } else if(type != null && boxTbDeviceId != null){
            clAlarmHistoryInfoDtoPage = clAlarmHistoryRepository
                    .findAllWithPaging(tenantId, viewed, type, boxTbDeviceId, startTs, endTs, pageable)
                    .map(this::toClAlarmHistoryInfoDto);
        } else if (type == null) {
            clAlarmHistoryInfoDtoPage = clAlarmHistoryRepository
                    .findAllWithPaging(tenantId, viewed, boxTbDeviceId, startTs, endTs, pageable)
                    .map(this::toClAlarmHistoryInfoDto);
        } else {
            clAlarmHistoryInfoDtoPage = clAlarmHistoryRepository
                    .findAllWithPaging(tenantId, viewed, type, startTs, endTs, pageable)
                    .map(this::toClAlarmHistoryInfoDto);
        }
        PageData<ClAlarmHistoryInfoDto> result = new PageData<>(clAlarmHistoryInfoDtoPage.getContent(),
                clAlarmHistoryInfoDtoPage.getTotalPages(),
                clAlarmHistoryInfoDtoPage.getTotalElements(),
                clAlarmHistoryInfoDtoPage.hasNext());
        return result;
    }

    @Override
    public int countTotalNotViewAlarm(UUID tenantId) {
        return clAlarmHistoryRepository.countAllByTenantIdAndViewed(tenantId, false);
    }

    @Override
    public void markALarmViewed(Set<UUID> alarmHistoryIds, UUID tenantId) {
        List<ClAlarmHistoryEntity> alarmHistoryEntities;
        if (alarmHistoryIds == null) {
            // mark all
            alarmHistoryEntities = clAlarmHistoryRepository.findAllByTenantIdAndViewed(tenantId, false);
        } else {
            alarmHistoryEntities = clAlarmHistoryRepository.findAllByTenantIdAndIdIn(tenantId, alarmHistoryIds);
        }
        alarmHistoryEntities.forEach(x -> {
            try {
                x.setViewed(true);
                clAlarmHistoryRepository.save(x);
            } catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    private ClAlarmHistoryEntity toClAlarmHistoryEntity(ClAlarmHistoryDto dto) {
        ClAlarmHistoryEntity result = new ClAlarmHistoryEntity();
        result.setId(dto.getId());
        result.setTenantId(dto.getTenantId());
        result.setTbAlarmId(dto.getTbAlarmId());
        result.setTbDeviceId(dto.getTbDeviceId());
        result.setDeviceType(dto.getDeviceType());
        result.setType(dto.getType());
        result.setViewed(dto.isViewed());
        result.setCreatedTime(dto.getCreatedTime());
        return result;
    }

    private ClAlarmHistoryDto toClAlarmHistoryDto(ClAlarmHistoryEntity entity) {
        ClAlarmHistoryDto result = new ClAlarmHistoryDto();
        result.setId(entity.getId());
        result.setTenantId(entity.getTenantId());
        result.setTbAlarmId(entity.getTbAlarmId());
        result.setTbDeviceId(entity.getTbDeviceId());
        result.setDeviceType(entity.getDeviceType());
        result.setType(entity.getType());
        result.setViewed(entity.isViewed());
        result.setCreatedTime(entity.getCreatedTime());
        return result;
    }

    private ClAlarmHistoryInfoDto toClAlarmHistoryInfoDto(ClAlarmHistoryEntity entity) {
        UUID tenantId = entity.getTenantId();
        ClAlarmHistoryInfoDto result = new ClAlarmHistoryInfoDto();
        result.setId(entity.getId());
        result.setTenantId(tenantId);
        result.setTbAlarmId(entity.getTbAlarmId());
        result.setTbDeviceId(entity.getTbDeviceId());
        result.setDeviceType(entity.getDeviceType());
        result.setType(entity.getType());
        result.setViewed(entity.isViewed());
        result.setCreatedTime(entity.getCreatedTime());

        // deviceName and box name:
        if (entity.getDeviceType().equals(ClDeviceConstant.DEVICE_TYPE_CAM)) {
            CameraEntity cameraEntity = cameraRepository.findByTenantIdAndTbDeviceId(tenantId, entity.getTbDeviceId());
            if (cameraEntity != null) {
                result.setDeviceName(cameraEntity.getCameraName());
                result.setBoxName(cameraEntity.getBoxEntity().getBoxName());
            }
        } else {
            BoxEntity boxEntity = boxRepository.findByTenantIdAndTbDeviceIdAndIsDeleteFalse(tenantId, entity.getTbDeviceId());
            if(boxEntity != null){
                result.setDeviceName(boxEntity.getBoxName());
            }
        }

        return result;
    }
}
