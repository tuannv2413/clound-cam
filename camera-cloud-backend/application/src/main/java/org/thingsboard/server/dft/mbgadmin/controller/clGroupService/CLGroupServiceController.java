package org.thingsboard.server.dft.mbgadmin.controller.clGroupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseMessageDto;
import org.thingsboard.server.dft.mbgadmin.dto.clGroupService.CLGroupServiceDetailDto;
import org.thingsboard.server.dft.mbgadmin.dto.clGroupService.MaxDayStorageDto;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseDataDto;
import org.thingsboard.server.dft.mbgadmin.service.clGroupService.CLGroupService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.annotation.security.PermitAll;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping(value = "/api/noauth/mange/group-service")
public class CLGroupServiceController extends BaseController {

    @Autowired
    private CLGroupService clGroupService;

    @PermitAll
    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getPage(
            @RequestParam(name = "page") Optional<Integer> page,
            @RequestParam(name = "pageSize") Optional<Integer> pageSize,
            @RequestParam(name = "maxDayStorage", required = false) Optional<Integer> maxDayStorage,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(name = "textSearch", required = false) Optional<String> textSearch,
            @RequestParam(name = "sortProperty", required = false, defaultValue = "createdTime") Optional<String> sortProperty,
            @RequestParam(name = "sortOrder", required = false, defaultValue = "desc") Optional<String> sortOrder
//            @RequestParam(name = "sortName", required = false) Optional<String> sortName,
//            @RequestParam(name = "sortMaxDayStorage", required = false) Optional<String> sortMaxDayStorage,
//            @RequestParam(name = "sortNote", required = false) Optional<String> sortNote,
//            @RequestParam(name = "sortActive", required = false) Optional<String> sortActive,
    ) {
        try {
            // sort 2
//            ResponseDataDto result = clGroupService.getPage(
//                    page.orElse(0), pageSize.orElse(10), maxDayStorage.orElse(0), active, textSearch.orElse("").trim(),
//                    sortName.orElse(null), sortMaxDayStorage.orElse(null), sortNote.orElse(null), sortActive.orElse(null));

            // sort 1
            ResponseDataDto result = clGroupService.getPage(
                    page.orElse(0), pageSize.orElse(10), maxDayStorage.orElse(0), active, textSearch.orElse("").trim(),
                    sortProperty.orElse("createdTime"), sortOrder.orElse("desc"));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Get Page GroupService Unsuccessful.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(49);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }
    }

    @PermitAll
    @GetMapping(value = "/find-resolution")
    @ResponseBody
    public ResponseEntity<?> findByResolution(@RequestParam(name = "page") Optional<Integer> page,
                                              @RequestParam(name = "pageSize") Optional<Integer> pageSize,
                                              @RequestParam(name = "resolution", required = false) Optional<String> resolution) {
        try {
            ResponseDataDto result = clGroupService.findByResolution(page.orElse(0), pageSize.orElse(10), resolution.orElse("").trim());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Get List MaxDatStorage Find By Resolution Unsuccessful.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(49);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }
    }

    @PermitAll
    @GetMapping(value = "/all")
    @ResponseBody
    public ResponseEntity<?> getAll() {
        try {
            List<CLGroupServiceDetailDto> result = clGroupService.getAll();
            if (result.size() > 0) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No Data!!!");
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Get List GroupService Unsuccessful.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(49);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }
    }

    @PermitAll
    @GetMapping(value = "/max-day-storage")
    @ResponseBody
    public ResponseEntity<?> getAllMaxDayStorage() {
        try {
            List<MaxDayStorageDto> result = clGroupService.getAllMaxDayStorage();
            if (result.size() > 0) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.ok(new ArrayList<>());
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Get List MaxDatStorage Unsuccessful.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(49);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }
    }

    @PermitAll
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> create(@RequestBody CLGroupServiceDetailDto clGroupServiceDetailDto) {
        try {
            Boolean checkName = clGroupService.checkExistsByName(clGroupServiceDetailDto.getName().trim());
            Boolean checkMaxDayStorage = clGroupService.checkExistsByMaxDayStorage(clGroupServiceDetailDto.getMaxDayStorage());

            if (checkName && !checkMaxDayStorage) {
                ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                responseMessageDto.setMessage("Tên dịch vụ đã tồn tại trên hệ thống.");
                responseMessageDto.setStatus(403);
                responseMessageDto.setErrorCode(48);
                responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
            }
            if (!checkName && checkMaxDayStorage) {
                ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                responseMessageDto.setMessage("Số ngày lưu trữ dịch vụ đã tồn tại trên hệ thống.");
                responseMessageDto.setStatus(403);
                responseMessageDto.setErrorCode(49);
                responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
            }
            if (checkName && checkMaxDayStorage) {
                ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                responseMessageDto.setMessage("Tên và Số ngày lưu trữ dịch vụ đã tồn tại trên hệ thống.");
                responseMessageDto.setStatus(403);
                responseMessageDto.setErrorCode(50);
                responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
            }

            CLGroupServiceDetailDto result = clGroupService.createOrUpdate(clGroupServiceDetailDto);
            if (result != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
            }else {
                ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                responseMessageDto.setMessage("Create New GroupService Unsuccessful.");
                responseMessageDto.setStatus(500);
                responseMessageDto.setErrorCode(50);
                responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
            }
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Create New GroupService Unsuccessful.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(49);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }
    }

    @PermitAll
    @PutMapping(value = "/{id}")
    @ResponseBody
    public ResponseEntity<?> update(@RequestBody CLGroupServiceDetailDto clGroupServiceDetailDto, @PathVariable("id") UUID id) {
        try {
            Boolean checkName = clGroupService.checkExistsByName(clGroupServiceDetailDto.getName().trim());
            Boolean checkMaxDayStorage = clGroupService.checkExistsByMaxDayStorage(clGroupServiceDetailDto.getMaxDayStorage());
            CLGroupServiceDetailDto checkUpdate = clGroupService.getById(id);
            if (checkUpdate == null) {
                ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                responseMessageDto.setMessage("GroupService Invalid.");
                responseMessageDto.setStatus(417);
                responseMessageDto.setErrorCode(47);
                responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
            }
            // ngày giống, tên không giống
            if (!clGroupServiceDetailDto.getName().trim().equals(checkUpdate.getName()) && clGroupServiceDetailDto.getMaxDayStorage() == checkUpdate.getMaxDayStorage()) {
                if (checkName) {
                    ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                    responseMessageDto.setMessage("Tên dịch vụ đã tồn tại trên hệ thống.");
                    responseMessageDto.setStatus(403);
                    responseMessageDto.setErrorCode(48);
                    responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
                }
            }
            // tên giống, ngày không giống
            if (clGroupServiceDetailDto.getName().trim().equals(checkUpdate.getName()) && clGroupServiceDetailDto.getMaxDayStorage() != checkUpdate.getMaxDayStorage()) {
                if (checkMaxDayStorage) {
                    ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                    responseMessageDto.setMessage("Số ngày lưu trữ dịch vụ đã tồn tại trên hệ thống.");
                    responseMessageDto.setStatus(403);
                    responseMessageDto.setErrorCode(49);
                    responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
                }
            }
            // ngày, và tên không giống
            if (!clGroupServiceDetailDto.getName().trim().equals(checkUpdate.getName()) && clGroupServiceDetailDto.getMaxDayStorage() != checkUpdate.getMaxDayStorage()){

                if (checkName && !checkMaxDayStorage) {
                    ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                    responseMessageDto.setMessage("Tên dịch vụ đã tồn tại trên hệ thống.");
                    responseMessageDto.setStatus(403);
                    responseMessageDto.setErrorCode(48);
                    responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
                }
                if (!checkName && checkMaxDayStorage) {
                    ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                    responseMessageDto.setMessage("Số ngày lưu trữ dịch vụ đã tồn tại trên hệ thống.");
                    responseMessageDto.setStatus(403);
                    responseMessageDto.setErrorCode(49);
                    responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
                }
                if (checkName && checkMaxDayStorage) {
                    ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                    responseMessageDto.setMessage("Tên và Số ngày lưu trữ dịch vụ đã tồn tại trên hệ thống.");
                    responseMessageDto.setStatus(403);
                    responseMessageDto.setErrorCode(50);
                    responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
                }
            }

            clGroupServiceDetailDto.setId(id);

            CLGroupServiceDetailDto result = clGroupService.createOrUpdate(clGroupServiceDetailDto);
            if (result != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
            }else {
                ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                responseMessageDto.setMessage("Update GroupService Unsuccessful.");
                responseMessageDto.setStatus(500);
                responseMessageDto.setErrorCode(50);
                responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
            }
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Update GroupService Unsuccessful.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(49);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }
    }

    @PermitAll
    @DeleteMapping("{id}")
    @ResponseBody
    public ResponseEntity<?> deleteById(@PathVariable("id") UUID id) {
        try {
            ResponseMessageDto result = clGroupService.deleteById(id);
            if(result.getErrorCode() == 51 || result.getErrorCode() == 49) {
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(result);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Xóa thất bại.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(49);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }
    }

    @PermitAll
    @GetMapping(value = "{id}")
    @ResponseBody
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
        try {
            return ResponseEntity.ok(clGroupService.getById(id));
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Get GroupService By ID Unsuccessful.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(49);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }
    }
}
