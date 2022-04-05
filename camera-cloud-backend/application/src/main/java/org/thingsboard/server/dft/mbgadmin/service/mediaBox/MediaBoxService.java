package org.thingsboard.server.dft.mbgadmin.service.mediaBox;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.mbgadmin.dto.mediaBox.MediaBoxDetailDto;
import org.thingsboard.server.dft.mbgadmin.dto.mediaBox.MediaBoxEditDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.UUID;

public interface MediaBoxService {
    MediaBoxEditDto createOrUpdate(MediaBoxEditDto mediaBoxEditDto, SecurityUser securityUser);

    PageData<MediaBoxEditDto> getPage(Pageable pageable, String textSearch, String type, String status, String firmwareVersion);

    PageData<MediaBoxEditDto> getCustomPageBySearch(Pageable pageable, String textSearch, String type, String status, String firmwareVersion, Boolean active);

    PageData<MediaBoxEditDto> getPageBySearch(Pageable pageable, String textSearch, String type, String status, String firmwareVersion, Boolean active);

    MediaBoxDetailDto getById(UUID id);

    void deleteById(UUID id);

    boolean checkExistSerialNumber(String serialNumber);

    boolean checkExistOriginSerialNumberAndIdNot(String serialNumber, UUID id);

    boolean checkExistOriginSerialNumber(String originOriginNumber);

    Resource downloadSample(String fileName);
}
