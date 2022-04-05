package org.thingsboard.server.dft.mbgadmin.service.clGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dft.mbgadmin.dao.clGroupService.CLGroupServiceDao;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseMessageDto;
import org.thingsboard.server.dft.mbgadmin.dto.clGroupService.CLGroupServiceDetailDto;
import org.thingsboard.server.dft.mbgadmin.dto.clGroupService.MaxDayStorageDto;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseDataDto;
import org.thingsboard.server.dft.mbgadmin.repository.clGroupService.CLGroupServiceRepository;

import java.util.*;

@Service
public class CLGroupServiceImpl implements CLGroupService {

    private CLGroupServiceDao clGroupServiceDao;
    private CLGroupServiceRepository clGroupServiceRepository;

    @Autowired
    public CLGroupServiceImpl(CLGroupServiceDao clGroupServiceDao, CLGroupServiceRepository clGroupServiceRepository) {
        this.clGroupServiceDao = clGroupServiceDao;
        this.clGroupServiceRepository = clGroupServiceRepository;
    }

    @Override
    public ResponseDataDto getPage(int page, int pageSize, Integer maxDayStorage, Boolean active, String textSearch, String sortProperty, String sortOrder) {
        // sort 2
//        HashMap<String, String> sortParam = new HashMap<>();
//        if (sortName != null) {
//            sortParam.put("name",sortName);
//        }
//        if (sortMaxDayStorage != null) {
//            sortParam.put("maxDayStorage",sortMaxDayStorage);
//        }
//        if (sortNote != null) {
//            sortParam.put("note",sortNote);
//        }
//        if (sortActive != null) {
//            sortParam.put("active",sortActive);
//        }
//        Sort sort = sort(sortParam);

        // sort 1
        Sort sort = null;
        if (sortOrder.equals("desc")) {
            sort = Sort.by(sortProperty).descending();
        }
        if (sortOrder.equals("asc")) {
            sort = Sort.by(sortProperty).ascending();
        }
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        return clGroupServiceDao.getPage(pageable, maxDayStorage, active, textSearch);
    }

    @Override
    public ResponseDataDto findByResolution(int page, int pageSize, String resolution) {
        Pageable pageable = PageRequest.of(page, pageSize);
        return clGroupServiceDao.findByResolution(pageable, resolution);
    }

    @Override
    public List<CLGroupServiceDetailDto> getAll() {
        return clGroupServiceDao.getAll();
    }

    @Override
    public List<MaxDayStorageDto> getAllMaxDayStorage() {
        return clGroupServiceDao.getAllMaxDayStorage();
    }

    @Override
    public ResponseMessageDto deleteById(UUID id) {
        return clGroupServiceDao.deleteById(id);
    }

    @Override
    public CLGroupServiceDetailDto getById(UUID id) {
        return clGroupServiceDao.getById(id);
    }

    @Override
    public CLGroupServiceDetailDto createOrUpdate(CLGroupServiceDetailDto clGroupServiceDetailDto) {
        return clGroupServiceDao.createOrUpdate(clGroupServiceDetailDto);
    }

    @Override
    public Boolean checkExistsByName(String name) {
        return clGroupServiceRepository.existsByName(name);
    }

    @Override
    public Boolean checkExistsByMaxDayStorage(Integer maxDayStorage) {
        return clGroupServiceRepository.existsByMaxDayStorage(maxDayStorage);
    }

//    public String sort(List<String> sortParam, int descOrAsc) {
//        String sort = "";
//        for (String param:sortParam) {
//            if (descOrAsc == 1) {
//                sort += "a." + param + " DESC, ";
//            }else {
//                sort = "a." + param + " ASC, ";
//            }
//        }
//        return sort.trim().substring(0, sort.lastIndexOf(","));
//    }

    public Sort sort(HashMap<String, String> sortParam) {
        Sort sort = Sort.by("createdTime").descending();
        for (Map.Entry<String, String> entry : sortParam.entrySet()) {
            if (entry.getValue().equals("1")) {
                sort = sort.and(Sort.by(entry.getKey()).descending());
            }
            if (entry.getValue().equals("2")) {
                sort = sort.and(Sort.by(entry.getKey()).ascending());
            }
        }
        return sort;
    }
}
