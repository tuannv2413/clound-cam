package org.thingsboard.server.dft.mbgadmin.controller.mediaBox;

import io.swagger.annotations.ApiOperation;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.mbgadmin.dto.mediaBox.ImportExcelResult;
import org.thingsboard.server.dft.mbgadmin.dto.mediaBox.MediaBoxEditDto;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import javax.annotation.security.PermitAll;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api/media-box")
public class MediaBoxController extends BaseController {

    @PermitAll
    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getAll(
            @RequestParam(name = "pageSize") int pageSize,
            @RequestParam(name = "page") int page,
            @RequestParam(required = false, defaultValue = "") String textSearch,
            @RequestParam(required = false, defaultValue = "") String type,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false, defaultValue = "") String firmwareVersion,
            @RequestParam(required = false, defaultValue = "partNumber") String sortProperty,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) throws ThingsboardException {
        try {
            Pageable pageable =
                    PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty);
            PageData<MediaBoxEditDto> pageData =
                    mediaBoxService.getPage(pageable, textSearch, type, status, firmwareVersion);
            return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @GetMapping("store")
    @ResponseBody
    public ResponseEntity<?> getCustomPageBySearch(
            @RequestParam(name = "pageSize") int pageSize,
            @RequestParam(name = "page") int page,
            @RequestParam(required = false, defaultValue = "") String textSearch,
            @RequestParam(required = false, defaultValue = "") String type,
            @RequestParam(required = false, defaultValue = "") String status,
            @RequestParam(required = false, defaultValue = "") String firmwareVersion,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false, defaultValue = "createdTime") String sortProperty,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) throws ThingsboardException {
        try {
            Pageable pageable =
                    PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty);
            PageData<MediaBoxEditDto> pageData =
                    mediaBoxService.getCustomPageBySearch(pageable, textSearch, type, status, firmwareVersion, active);
            return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @GetMapping("page")
    @ResponseBody
    public ResponseEntity<?> getAllBySearch(
        @RequestParam(name = "pageSize") int pageSize,
        @RequestParam(name = "page") int page,
        @RequestParam(required = false, defaultValue = "") String textSearch,
        @RequestParam(required = false, defaultValue = "") String type,
        @RequestParam(required = false, defaultValue = "") String status,
        @RequestParam(required = false, defaultValue = "") String firmwareVersion,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false, defaultValue = "createdTime") String sortProperty,
        @RequestParam(required = false, defaultValue = "desc") String sortOrder) throws ThingsboardException {
        try {
            Pageable pageable =
                PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty);
            PageData<MediaBoxEditDto> pageData =
                mediaBoxService.getPageBySearch(pageable, textSearch, type, status, firmwareVersion, active);
            return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @GetMapping(value="{id}")
    @ResponseBody
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) throws ThingsboardException {
        try {
            return new ResponseEntity<>(checkNotNull(mediaBoxService.getById(id)), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @PostMapping
    @ResponseBody
    @ApiOperation(value = "Thêm mới hoặc update(id != null) example entity",
            notes = "Hệ thông sẽ tự điều chỉnh createdTime, updatedTime, createdBy, updatedBy")
    public ResponseEntity<?> createOrUpdate(@RequestBody MediaBoxEditDto mediaBoxEditDto) throws ThingsboardException {
        try {
            SecurityUser securityUser = getCurrentUser();
            mediaBoxEditDto.setOriginSerialNumber(mediaBoxEditDto.getOriginSerialNumber().trim());
            mediaBoxEditDto.setSerialNumber(mediaBoxEditDto.getSerialNumber().trim());
            if (mediaBoxEditDto.getId() == null) {
                if (mediaBoxService.checkExistOriginSerialNumber(mediaBoxEditDto.getOriginSerialNumber())) {
                    throw new ThingsboardException("Serial number nhà sản xuất đã tồn tại!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                }
                while(mediaBoxService.checkExistSerialNumber(mediaBoxEditDto.getSerialNumber())) {
                    String s = mediaBoxEditDto.getSerialNumber();
                    String firstPart = s.substring(0, 5);
                    int randomNumber = (int) Math.floor(((Math.random() * 8999999) + 1000000));
                    String newSerialNumber = firstPart + randomNumber;
                    mediaBoxEditDto.setSerialNumber(newSerialNumber);
                }
                mediaBoxEditDto.setStatus("TAI_KHO");
            } else {
                if (mediaBoxService.checkExistOriginSerialNumberAndIdNot(mediaBoxEditDto.getOriginSerialNumber(), mediaBoxEditDto.getId())) {
                    throw new ThingsboardException("Serial number nhà sản xuất đã tồn tại!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                }
            }
            return new ResponseEntity<>(checkNotNull(mediaBoxService.createOrUpdate(mediaBoxEditDto, securityUser)), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @PostMapping("import-excel")
    @ResponseBody
    @ApiOperation(value = "Thêm mới MediaBox từ File Excel",
            notes = "Hệ thông sẽ tự điều chỉnh createdTime, updatedTime, createdBy, updatedBy")
    public ResponseEntity<?> createBoxFromFile(@RequestBody List<MediaBoxEditDto> mediaBoxsEditDtos) throws ThingsboardException {
        try {
            SecurityUser securityUser = getCurrentUser();
//            SecurityUser fakeUser = new SecurityUser();
//            fakeUser.setId(new UserId(UUID.randomUUID()));
            ImportExcelResult importExcelResult = new ImportExcelResult();
            importExcelResult.setTotal(mediaBoxsEditDtos.size());
            int success = 0;
            int failed = 0;
            List<Integer> failedRow = new ArrayList<>();
            for(MediaBoxEditDto mediaBox: mediaBoxsEditDtos) {
                try {
                    if (mediaBox.getOriginSerialNumber() == null || mediaBox.getType() == null || mediaBoxService.checkExistOriginSerialNumber(mediaBox.getOriginSerialNumber())) {
                        throw new ThingsboardException("Error", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                    }
                    while(mediaBoxService.checkExistSerialNumber(mediaBox.getSerialNumber())) {
                        String s = mediaBox.getSerialNumber();
                        String firstPart = s.substring(0, 5);
                        int randomNumber = (int) Math.floor(((Math.random() * 8999999) + 1000000));
                        String newSerialNumber = firstPart + randomNumber;
                        mediaBox.setSerialNumber(newSerialNumber);
                    }
                    mediaBox.setStatus("TAI_KHO");
                    checkNotNull(mediaBoxService.createOrUpdate(mediaBox, securityUser));
                    success++;
                } catch (Exception e) {
                    failed++;
                    failedRow.add(mediaBoxsEditDtos.indexOf(mediaBox) + 1);
                }
            }
            importExcelResult.setSuccess(success);
            importExcelResult.setFail(failed);
            importExcelResult.setListFail(failedRow);
            return new ResponseEntity<>(checkNotNull(importExcelResult), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @DeleteMapping("{id}")
    @ResponseBody
    public ResponseEntity<?> deleteById(@PathVariable("id") UUID id) throws ThingsboardException {
        try {
            mediaBoxService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @GetMapping("download")
    public ResponseEntity<?> downloadSample(
            @RequestParam("taiLieu") String fileName
    ) throws ThingsboardException {
        try {
            Resource file = mediaBoxService.downloadSample(fileName);
            Path path = file.getFile().toPath();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(path))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
