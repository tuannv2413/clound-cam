package org.thingsboard.server.dft.mbgadmin.dao.mediaBox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.entity.box.BoxEntity;
import org.thingsboard.server.dft.enduser.repository.box.BoxRepository;
import org.thingsboard.server.dft.mbgadmin.dto.mediaBox.MediaBoxDetailDto;
import org.thingsboard.server.dft.mbgadmin.dto.mediaBox.MediaBoxEditDto;
import org.thingsboard.server.dft.mbgadmin.entity.mediaBox.MediaBoxEntity;
import org.thingsboard.server.dft.mbgadmin.repository.mediaBox.MediaBoxRepository;
import org.thingsboard.server.dft.mbgadmin.service.mediaBox.MediaBoxService;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MediaBoxDaoImpl implements MediaBoxDao {
    private final MediaBoxRepository mediaBoxRepository;
    private final BoxRepository boxRepository;

    @Autowired
    public MediaBoxDaoImpl(MediaBoxRepository mediaBoxRepository, BoxRepository boxRepository) {
        this.mediaBoxRepository = mediaBoxRepository;
        this.boxRepository = boxRepository;
    }

    @Override
    public MediaBoxEditDto createOrUpdate(MediaBoxEditDto mediaBoxEditDto, SecurityUser securityUser) {
        MediaBoxEntity mediaBoxEntity = new MediaBoxEntity();
        if (mediaBoxEditDto.getId() != null) {
            Optional<MediaBoxEntity> optionalMediaBoxEntity = mediaBoxRepository.findById(mediaBoxEditDto.getId());
           if (optionalMediaBoxEntity.isPresent()) {
               mediaBoxEntity = optionalMediaBoxEntity.get();
               mediaBoxEntity.setUpdatedTime(mediaBoxEditDto.getUpdatedTime());
               mediaBoxEntity.setUpdatedBy(mediaBoxEditDto.getUpdatedBy());
           } else {
               mediaBoxEntity.setId(mediaBoxEditDto.getId());
               mediaBoxEntity.setCreatedBy(mediaBoxEditDto.getCreatedBy());
               mediaBoxEntity.setCreatedTime(mediaBoxEditDto.getCreatedTime());
               mediaBoxEntity.setSerialNumber(mediaBoxEditDto.getSerialNumber());
           }
        } else {
            mediaBoxEntity.setTenantId(securityUser.getTenantId().getId());
            mediaBoxEntity.setId(UUID.randomUUID());
            mediaBoxEntity.setCreatedBy(mediaBoxEditDto.getCreatedBy());
            mediaBoxEntity.setCreatedTime(mediaBoxEditDto.getCreatedTime());
            mediaBoxEntity.setSerialNumber(mediaBoxEditDto.getSerialNumber());
        }
        mediaBoxEntity.setPartNumber(mediaBoxEditDto.getPartNumber());
        mediaBoxEntity.setFirmwareVersion(mediaBoxEditDto.getFirmwareVersion());
//        mediaBoxEntity.setSerialNumber(mediaBoxEditDto.getSerialNumber());
        mediaBoxEntity.setConsignment(mediaBoxEditDto.getConsignment());
        mediaBoxEntity.setOriginSerialNumber((mediaBoxEditDto.getOriginSerialNumber()));
        mediaBoxEntity.setType(mediaBoxEditDto.getType());
        mediaBoxEntity.setStatus(mediaBoxEditDto.getStatus());
        mediaBoxEntity = mediaBoxRepository.save(mediaBoxEntity);
        return new MediaBoxEditDto(mediaBoxEntity);
    }

    @Override
    public PageData<MediaBoxEditDto> getPage(Pageable pageable, String textSearch, String type, String status, String firmwareVersion) {
        Page<MediaBoxEditDto> mediaBoxDtoPage = mediaBoxRepository.findAllByName(pageable, textSearch, type, status, firmwareVersion).map(MediaBoxEditDto::new);
        return new PageData<>(mediaBoxDtoPage.getContent(), mediaBoxDtoPage.getTotalPages(),
                mediaBoxDtoPage.getTotalElements(), mediaBoxDtoPage.hasNext());
    }

    @Override
    public PageData<MediaBoxEditDto> getCustomPageBySearch(Pageable pageable, String textSearch, String type, String status, String firmwareVersion, Boolean active) {
        Page<MediaBoxEditDto> mediaBoxDtoPage = mediaBoxRepository.findCustomBoxBySearch(pageable, textSearch, type, status, firmwareVersion, active)
                .map(MediaBoxEditDto::new);
        return new PageData<>(mediaBoxDtoPage.getContent(), mediaBoxDtoPage.getTotalPages(),
                mediaBoxDtoPage.getTotalElements(), mediaBoxDtoPage.hasNext());
    }

    @Override
    public PageData<MediaBoxEditDto> getPageBySearch(Pageable pageable, String textSearch, String type, String status, String firmwareVersion, Boolean active) {
        Page<MediaBoxEditDto> mediaBoxDtoPage = mediaBoxRepository.findAllBySearch(pageable, textSearch, type, status, firmwareVersion, active)
            .map(MediaBoxEditDto::new);
        List<MediaBoxEditDto> mediaBoxEditDtos = mediaBoxDtoPage.getContent();
        for(MediaBoxEditDto mediaBox: mediaBoxEditDtos) {
            BoxEntity boxEntity = boxRepository.findDistinctBySerialNumberAndIsDeleteFalse(mediaBox.getSerialNumber());
            if (boxEntity != null) {
                mediaBox.setBoxId(boxEntity.getId());
            }
        }
//        return new PageData<>(mediaBoxDtoPage.getContent(), mediaBoxDtoPage.getTotalPages(),
        return new PageData<>(mediaBoxEditDtos, mediaBoxDtoPage.getTotalPages(),
            mediaBoxDtoPage.getTotalElements(), mediaBoxDtoPage.hasNext());
    }

    @Override
    public MediaBoxDetailDto getById(UUID id) {
        MediaBoxEntity mediaBoxEntity;
        Optional<MediaBoxEntity> optionalMediaBoxEntity = mediaBoxRepository.findById(id);
        if (optionalMediaBoxEntity.isPresent()) {
            mediaBoxEntity = optionalMediaBoxEntity.get();
            BoxEntity boxEntity = boxRepository.findDistinctBySerialNumberAndIsDeleteFalse(mediaBoxEntity.getSerialNumber());
            MediaBoxDetailDto mediaBoxEditDto = new MediaBoxDetailDto(mediaBoxEntity);
            if (boxEntity != null) {
                mediaBoxEditDto.setBoxId(boxEntity.getId());
            }
            return mediaBoxEditDto;
        } else {
            return null;
        }
    }

    @Override
    public void deleteById(UUID id) {
        mediaBoxRepository.deleteById(id);
    }

    @Override
    public boolean checkSerialNumberExist(String serialNumber) {
        return mediaBoxRepository.existsBySerialNumber(serialNumber);
    }

    @Override
    public boolean checkSerialNumberExistAndIdNot(String serialNumber, UUID id) {
        return mediaBoxRepository.existsBySerialNumberAndIdNot(serialNumber, id);
    }

    @Override
    public boolean checkOriginSerialNumberExistAndIdNot(String originSerialNumber, UUID id) {
        return mediaBoxRepository.existsByOriginSerialNumberAndIdNot(originSerialNumber, id);
    }

    @Override
    public boolean checkOriginSerialNumberExist(String originSerialNumber) {
        return mediaBoxRepository.existsByOriginSerialNumber(originSerialNumber);
    }
}
