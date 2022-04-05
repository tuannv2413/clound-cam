package org.thingsboard.server.dft.enduser.controller.user;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.enduser.dto.camera.CustomerCameraPermissionDto;
import org.thingsboard.server.dft.enduser.dto.camera.CustomerCameraPermissionSaveDto;
import org.thingsboard.server.dft.enduser.dto.user.EndUserDto;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api/end-user")
public class EndUserController extends BaseController {

    @PermitAll
    @ResponseBody
    @ApiOperation(value = "Lấy 1 page user entity",
            notes = "")
    @GetMapping("/search")
    public ResponseEntity<?> getAll(
            @RequestParam(name = "pageSize") int pageSize,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "active", required = false, defaultValue = "0") int active,
            @RequestParam(required = false, defaultValue = "") String textSearch,
            @RequestParam(required = false, defaultValue = "createdTime") String sortProperty,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder
    ) {
        try {
            SecurityUser securityUser = getCurrentUser();
            Pageable pageable =
                    PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty);
            PageData<EndUserDto> pageData =
                    endUserService.getPage(pageable, textSearch, active, securityUser);
            return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @PermitAll
    @DeleteMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "Xóa user entity by id (UUID)",
            notes = "")
    public ResponseEntity<?> deleteById(@PathVariable("id") UUID id) {
        try {
            SecurityUser securityUser = getCurrentUser();
            if (securityUser.getAuthority().equals(Authority.CUSTOMER_USER)) {
                throw new ThingsboardException("No permission!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
            }
            endUserService.deleteById(id, securityUser);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @PermitAll
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "Lấy thông tin user entity theo id (UUID)",
            notes = "")
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
        try {
            SecurityUser securityUser = getCurrentUser();
            return new ResponseEntity<>(checkNotNull(endUserService.getById(id, securityUser)), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @PostMapping("/create")
    @ResponseBody
    @ApiOperation(value = "Thêm mới user entity",
            notes = "Hệ thông sẽ tự điều chỉnh createdTime, updatedTime, createdBy, updatedBy")
    public ResponseEntity<?> create(@Valid @RequestBody EndUserDto exampleDto) throws ThingsboardException {
        try {
            SecurityUser securityUser = getCurrentUser();
            if (securityUser.getAuthority().equals(Authority.CUSTOMER_USER)) {
                throw new ThingsboardException("No permission!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
            }
            return new ResponseEntity<>(checkNotNull(endUserService.createOrUpdate(exampleDto, securityUser)), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
    @PutMapping("/update/{id}")
    @ResponseBody
    @ApiOperation(value = "update(id != null) user entity",
            notes = "Hệ thông sẽ tự điều chỉnh createdTime, updatedTime, createdBy, updatedBy")
    public ResponseEntity<?> update(@PathVariable("id") UUID id, @Valid @RequestBody EndUserDto exampleDto) throws ThingsboardException {
        try {
            SecurityUser securityUser = getCurrentUser();
            if (securityUser.getAuthority().equals(Authority.CUSTOMER_USER)) {
                throw new ThingsboardException("No permission!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
            }
            exampleDto.setId(id);
            return new ResponseEntity<>(checkNotNull(endUserService.createOrUpdate(exampleDto, securityUser)), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @ResponseBody
    @ApiOperation(value = "Lấy list các camera đc phân quyền",
            notes = "if want to us Không chọn nhóm nào noneGroup = 2")
    @GetMapping("/camera")
    public ResponseEntity<?> getAllCustomerCameraPermission(
            @RequestParam(name = "userID", required = false) UUID userID,
            @RequestParam(name = "boxId", required = false) UUID boxId,
            @RequestParam(name = "groupId", required = false) UUID groupId,
            @RequestParam(name = "cameraName", required = false) String cameraName,
            @RequestParam(required = false, name = "noneGroup", defaultValue = "1") int noneGroup) throws ThingsboardException {
        try {
            SecurityUser securityUser = getCurrentUser();
            List<CustomerCameraPermissionDto> permission =
                    customerCameraPermissionService.getAllCustomerCameraPermission(boxId, groupId, userID, cameraName, noneGroup, securityUser.getTenantId().getId());
            return new ResponseEntity<>(permission, HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @ResponseBody
    @ApiOperation(value = "Lưu thông camera được phân quyền",
            notes = "")
    @PostMapping("/camera")
    public ResponseEntity<?> saveCustomerCameraPermission(@RequestBody CustomerCameraPermissionSaveDto request) throws ThingsboardException {
        try {
            SecurityUser securityUser = getCurrentUser();
            if (securityUser.getAuthority().equals(Authority.CUSTOMER_USER)) {
                throw new ThingsboardException("No permission!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
            }
            List<CustomerCameraPermissionDto> permission =
                    customerCameraPermissionService.saveCustomerCameraPermission(request, securityUser.getUuidId());
            return new ResponseEntity<>(checkNotNull(permission), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
