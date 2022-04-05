package org.thingsboard.server.dft.mbgadmin.service.clGroupService;

import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseMessageDto;
import org.thingsboard.server.dft.mbgadmin.dto.clGroupService.CLGroupServiceDetailDto;
import org.thingsboard.server.dft.mbgadmin.dto.clGroupService.MaxDayStorageDto;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseDataDto;

import java.util.List;
import java.util.UUID;

public interface CLGroupService {
//    ResponseDataDto getPage(int page, int pageSize, Integer maxDayStorage, Boolean active, String textSearch, String sortName, String sortMaxDayStorage, String sortNote, String sortActive);

    ResponseDataDto getPage(int page, int pageSize, Integer maxDayStorage, Boolean active, String textSearch, String sortProperty, String sortOrder);

    ResponseDataDto findByResolution(int page, int pageSize, String resolution);

    List<CLGroupServiceDetailDto> getAll();

    List<MaxDayStorageDto> getAllMaxDayStorage();

    ResponseMessageDto deleteById(UUID id);

    CLGroupServiceDetailDto getById(UUID id);

    CLGroupServiceDetailDto createOrUpdate(CLGroupServiceDetailDto clGroupServiceDetailDto);

    Boolean checkExistsByName(String name);

    Boolean checkExistsByMaxDayStorage(Integer maxDayStorage);
}
