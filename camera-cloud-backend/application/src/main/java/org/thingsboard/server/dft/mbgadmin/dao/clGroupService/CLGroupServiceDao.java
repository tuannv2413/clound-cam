package org.thingsboard.server.dft.mbgadmin.dao.clGroupService;

import org.springframework.data.domain.Pageable;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseMessageDto;
import org.thingsboard.server.dft.mbgadmin.dto.clGroupService.CLGroupServiceDetailDto;
import org.thingsboard.server.dft.mbgadmin.dto.clGroupService.MaxDayStorageDto;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseDataDto;

import java.util.List;
import java.util.UUID;

public interface CLGroupServiceDao {
    ResponseDataDto getPage(Pageable pageable, Integer maxDayStorage, Boolean active, String textSearch);

    ResponseDataDto findByResolution(Pageable pageable, String resolution);

    List<CLGroupServiceDetailDto> getAll();

    List<MaxDayStorageDto> getAllMaxDayStorage();

    ResponseMessageDto deleteById(UUID id);

    CLGroupServiceDetailDto getById(UUID id);

    CLGroupServiceDetailDto createOrUpdate(CLGroupServiceDetailDto clGroupServiceDetailDto);
}
