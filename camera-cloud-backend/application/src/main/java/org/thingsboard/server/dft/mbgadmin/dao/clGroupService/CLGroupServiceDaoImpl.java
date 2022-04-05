package org.thingsboard.server.dft.mbgadmin.dao.clGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseMessageDto;
import org.thingsboard.server.dft.mbgadmin.dto.clGroupService.CLGroupServiceDetailDto;
import org.thingsboard.server.dft.mbgadmin.dto.clGroupService.MaxDayStorageDto;
import org.thingsboard.server.dft.mbgadmin.dto.clServiceOption.CLServiceOptionDto;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseDataDto;
import org.thingsboard.server.dft.mbgadmin.entity.clGroupService.CLGroupServiceEntity;
import org.thingsboard.server.dft.mbgadmin.entity.clServiceOption.CLServiceOptionEntity;
import org.thingsboard.server.dft.mbgadmin.repository.clGroupService.CLGroupServiceRepository;
import org.thingsboard.server.dft.mbgadmin.repository.clServiceOption.CLServiceOptionRepository;

import java.sql.Timestamp;
import java.util.*;

@Component
public class CLGroupServiceDaoImpl implements CLGroupServiceDao {

    private CLGroupServiceRepository clGroupServiceRepository;
    private CLServiceOptionRepository clServiceOptionRepository;

    @Autowired
    public CLGroupServiceDaoImpl(CLGroupServiceRepository clGroupServiceRepository, CLServiceOptionRepository clServiceOptionRepository) {
        this.clGroupServiceRepository = clGroupServiceRepository;
        this.clServiceOptionRepository = clServiceOptionRepository;
    }

    @Override
    @Transactional
    public ResponseDataDto getPage(Pageable pageable, Integer maxDayStorage, Boolean active, String textSearch) {
        try {
            List<CLGroupServiceDetailDto> clGroupServiceDetailDtos = new ArrayList<>();
            Page<CLGroupServiceEntity> clGroupServiceEntities = clGroupServiceRepository.findALLAndSearch(pageable, maxDayStorage, active, textSearch);
            if (clGroupServiceEntities.getSize() > 0) {
                for (CLGroupServiceEntity clg : clGroupServiceEntities) {
                    List<CLServiceOptionDto> clServiceOptionDtos = new ArrayList<>();
                    for (CLServiceOptionEntity cls : clg.getClServiceOptionEntities()) {
                        CLServiceOptionDto clServiceOptionDto = new CLServiceOptionDto(cls);
                        clServiceOptionDtos.add(clServiceOptionDto);
                    }
                    CLGroupServiceDetailDto clGroupServiceDetailDto = new CLGroupServiceDetailDto(clg, clServiceOptionDtos);
                    clGroupServiceDetailDtos.add(clGroupServiceDetailDto);
                }
            }
            ResponseDataDto result = ResponseDataDto.builder()
                    .data(clGroupServiceDetailDtos)
                    .totalPages(clGroupServiceEntities.getTotalPages())
                    .totalElements((int) clGroupServiceEntities.getTotalElements())
                    .hasNext(pageable.getPageNumber() == (clGroupServiceEntities.getTotalPages() - 1) ? true : false)
                    .build();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResponseDataDto findByResolution(Pageable pageable, String resolution) {
        try {
            List<CLGroupServiceDetailDto> clGroupServiceDetailDtos = new ArrayList<>();
            Page<CLGroupServiceEntity> clGroupServiceEntities = clGroupServiceRepository.findResolution(pageable, resolution);
            if (clGroupServiceEntities.getSize() > 0) {
                for (CLGroupServiceEntity clg : clGroupServiceEntities) {
                    List<CLServiceOptionDto> clServiceOptionDtos = new ArrayList<>();
                    for (CLServiceOptionEntity cls : clg.getClServiceOptionEntities()) {
                        CLServiceOptionDto clServiceOptionDto = new CLServiceOptionDto(cls);
                        clServiceOptionDtos.add(clServiceOptionDto);
                    }
                    CLGroupServiceDetailDto clGroupServiceDetailDto = new CLGroupServiceDetailDto(clg, clServiceOptionDtos);
                    clGroupServiceDetailDtos.add(clGroupServiceDetailDto);
                }
            }
            ResponseDataDto result = ResponseDataDto.builder()
                    .data(clGroupServiceDetailDtos)
                    .totalPages(clGroupServiceEntities.getTotalPages())
                    .totalElements((int) clGroupServiceEntities.getTotalElements())
                    .hasNext(pageable.getPageNumber() == (clGroupServiceEntities.getTotalPages() - 1) ? true : false)
                    .build();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<CLGroupServiceDetailDto> getAll() {
        try {
            List<CLGroupServiceDetailDto> result = new ArrayList<>();
            List<CLGroupServiceEntity> clGroupServiceEntities = clGroupServiceRepository.findAll();
            if (clGroupServiceEntities.size() > 0) {
                for (CLGroupServiceEntity clg : clGroupServiceEntities) {
                    List<CLServiceOptionDto> clServiceOptionDtos = new ArrayList<>();
                    for (CLServiceOptionEntity cls : clg.getClServiceOptionEntities()) {
                        CLServiceOptionDto clServiceOptionDto = new CLServiceOptionDto(cls);
                        clServiceOptionDtos.add(clServiceOptionDto);
                    }
                    CLGroupServiceDetailDto clGroupServiceDetailDto = new CLGroupServiceDetailDto(clg, clServiceOptionDtos);
                    result.add(clGroupServiceDetailDto);
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<MaxDayStorageDto> getAllMaxDayStorage() {
        try {
            List<MaxDayStorageDto> result = new ArrayList<>();
            List<CLGroupServiceEntity> clGroupServiceEntities = clGroupServiceRepository.findAll(Sort.by(Sort.Direction.ASC, "maxDayStorage"));
            if (clGroupServiceEntities.size() > 0) {
                for (CLGroupServiceEntity clg : clGroupServiceEntities) {
                    MaxDayStorageDto maxDayStorageDto = MaxDayStorageDto.builder().maxDayStorage(clg.getMaxDayStorage()).build();
                    result.add(maxDayStorageDto);
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    public ResponseMessageDto deleteById(UUID id) {
        try {
            Optional<CLGroupServiceEntity> clGroupServiceEntity = clGroupServiceRepository.findById(id);
            for (CLServiceOptionEntity cls : clGroupServiceEntity.get().getClServiceOptionEntities()) {
                List<String> listCheck = clServiceOptionRepository.checkGroupService(cls.getId());
                if (listCheck.size() > 0) {
                    ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                    responseMessageDto.setMessage("Dịch vụ đã được sử dụng.");
                    responseMessageDto.setStatus(417);
                    responseMessageDto.setErrorCode(51);
                    responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                    return responseMessageDto;
                }
            }
            clGroupServiceRepository.deleteById(id);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Xóa thành công");
            responseMessageDto.setStatus(200);
            responseMessageDto.setErrorCode(0);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return responseMessageDto;
        } catch (Exception e) {
            e.printStackTrace();
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Xóa thất bại. " + e.getMessage());
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(49);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return responseMessageDto;
        }
    }

    @Override
    @Transactional
    public CLGroupServiceDetailDto getById(UUID id) {
        try {
            Optional<CLGroupServiceEntity> clGroupServiceEntity = clGroupServiceRepository.findById(id);
            if (clGroupServiceEntity.isEmpty()) {
                return null;
            }
            List<CLServiceOptionDto> clServiceOptionDtos = new ArrayList<>();
            for (CLServiceOptionEntity cls : clGroupServiceEntity.get().getClServiceOptionEntities()) {
                CLServiceOptionDto clServiceOptionDto = new CLServiceOptionDto(cls);
                clServiceOptionDtos.add(clServiceOptionDto);
            }
            CLGroupServiceDetailDto result = new CLGroupServiceDetailDto(clGroupServiceEntity.get(), clServiceOptionDtos);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional
    public CLGroupServiceDetailDto createOrUpdate(CLGroupServiceDetailDto clGroupServiceDetailDto) {
        try {
            // Insert table cl_group_service
            CLGroupServiceEntity clGroupServiceEntity = new CLGroupServiceEntity();
            if (clGroupServiceDetailDto.getId() != null) {
                Optional<CLGroupServiceEntity> clGroupServiceDB = clGroupServiceRepository.findById(clGroupServiceDetailDto.getId());
                clGroupServiceEntity.setId(clGroupServiceDetailDto.getId());
                clGroupServiceEntity.setUpdatedTime(new Date().getTime());
                clGroupServiceEntity.setUpdatedBy(clGroupServiceDetailDto.getUpdatedBy());
                clGroupServiceEntity.setCreatedTime(clGroupServiceDB.get().getCreatedTime());
                clGroupServiceEntity.setCreatedBy(clGroupServiceDetailDto.getCreatedBy());
                clGroupServiceEntity.setActive(clGroupServiceDetailDto.getActive());
            } else {
                clGroupServiceEntity.setId(UUID.randomUUID());
                clGroupServiceEntity.setCreatedTime(new Date().getTime());
                clGroupServiceEntity.setCreatedBy(clGroupServiceDetailDto.getCreatedBy());
                clGroupServiceEntity.setActive(true);
            }
            clGroupServiceEntity.setName(clGroupServiceDetailDto.getName().trim());
            clGroupServiceEntity.setNote(clGroupServiceDetailDto.getNote().trim());
            clGroupServiceEntity.setMaxDayStorage(clGroupServiceDetailDto.getMaxDayStorage());
            CLGroupServiceEntity clGroupServiceEntityResponse = new CLGroupServiceEntity();
            clGroupServiceEntityResponse = clGroupServiceRepository.save(clGroupServiceEntity);

            // Insert table cl_service_option
            List<CLServiceOptionEntity> clServiceOptionEntities = new ArrayList<>();
            for (CLServiceOptionDto cls : clGroupServiceDetailDto.getClServiceOptionDtos()) {
                CLServiceOptionEntity clServiceOptionEntity = new CLServiceOptionEntity();
                if (cls.getId() != null) {
                    clServiceOptionEntity.setId(cls.getId());
                } else {
                    clServiceOptionEntity.setId(UUID.randomUUID());
                }
                clServiceOptionEntity.setPrice(cls.getPrice());
                clServiceOptionEntity.setResolution(cls.getResolution());
                clServiceOptionEntity.setClGroupServiceEntity(clGroupServiceEntityResponse);
                clServiceOptionEntities.add(clServiceOptionEntity);
            }
            List<CLServiceOptionEntity> clServiceOptionEntityResponse = clServiceOptionRepository.saveAll(clServiceOptionEntities);

            List<CLServiceOptionDto> clServiceOptionDtos = new ArrayList<>();
            for (CLServiceOptionEntity cls : clServiceOptionEntityResponse) {
                CLServiceOptionDto clServiceOptionDto = new CLServiceOptionDto(cls);
                clServiceOptionDtos.add(clServiceOptionDto);
            }
            CLGroupServiceDetailDto result = new CLGroupServiceDetailDto(clGroupServiceEntityResponse, clServiceOptionDtos);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
