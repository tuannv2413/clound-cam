package org.thingsboard.server.dft.enduser.controller.cameraGroup;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.enduser.dto.cameraGroup.CameraGroupDto;
import org.thingsboard.server.dft.enduser.dto.cameraGroup.CameraGroupList;
import org.thingsboard.server.dft.enduser.dto.cameraGroup.CameraGroupResponse;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import javax.annotation.security.PermitAll;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api/camera-group")
public class CameraGroupController extends BaseController {

    @PermitAll
    @PostMapping
    @ResponseBody
    @ApiOperation(value = "Lưu thông tin nhóm camera", notes = "")
    public ResponseEntity<?> save(@RequestBody CameraGroupDto cameraGroupDto) throws ThingsboardException {
        try {
            SecurityUser securityUser = getCurrentUser();
            if (securityUser.getAuthority().equals(Authority.CUSTOMER_USER)) {
                throw new ThingsboardException("No permission!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
            }
            CameraGroupResponse cameraGroupResponse = cameraGroupService.save(cameraGroupDto, securityUser);
            return new ResponseEntity<>(checkNotNull(cameraGroupResponse), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @PutMapping("/default")
    @ResponseBody
    @ApiOperation(value = "Chỉnh sửa nhóm mặc định của nhóm camera", notes = "Truyền ID của nhóm camera cần update, và load lại list")
    public ResponseEntity<?> setDefault(
            @RequestParam(name = "id") UUID id,
            @RequestParam(name = "isDefault") Boolean isDefault) throws ThingsboardException {
        try {
            SecurityUser securityUser = getCurrentUser();
            CameraGroupResponse cameraGroupResponse = cameraGroupService.setDefault(securityUser, id, isDefault);
            return new ResponseEntity<>(cameraGroupResponse, HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @DeleteMapping("/{id}")
    @PermitAll
    @ResponseBody
    @ApiOperation(value = "Xóa 1 nhóm camera", notes = "Sau khi xóa 1 nhóm camera, các camera trong nhóm đó sẽ update lại")
    public ResponseEntity<?> delete(@PathVariable UUID id) throws ThingsboardException {
        try {
            SecurityUser securityUser = getCurrentUser();
            if (securityUser.getAuthority().equals(Authority.CUSTOMER_USER)) {
                throw new ThingsboardException("No permission!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
            }
            cameraGroupService.delete(securityUser, id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }


    @PermitAll
    @ResponseBody
    @ApiOperation(value = "Lấy 1 page camera", notes = "")
    @GetMapping("/search")
    public ResponseEntity<?> getAll(@RequestParam(name = "pageSize") int pageSize, @RequestParam(name = "page") int page, @RequestParam(required = false, defaultValue = "") String textSearch, @RequestParam(required = false, defaultValue = "isDefault") String sortProperty, @RequestParam(required = false, defaultValue = "desc") String sortOrder) throws ThingsboardException {
        try {
            SecurityUser securityUser = getCurrentUser();
            Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty, "isDefault", "createdTime");
            PageData<CameraGroupList> pageData = cameraGroupService.getPage(securityUser, pageable, textSearch);
            return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @ResponseBody
    @ApiOperation(value = "Get all camera group list", notes = "")
    @GetMapping("/no-page")
    public ResponseEntity<?> getAllCameraGroupList() throws ThingsboardException {
        try {
            SecurityUser securityUser = getCurrentUser();
            return new ResponseEntity<>(checkNotNull(cameraGroupService.getAll(securityUser)), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @ResponseBody
    @ApiOperation(value = "Get all camera by group id", notes = "" + "if want to us Không chọn nhóm nào noneGroup = 2" + "")
    @GetMapping("/camera")
    public ResponseEntity<?> getAllCameraByGroupId(
            @RequestParam(required = false, name = "groupId") UUID groupId,
            @RequestParam(required = false, name = "cameraName") String cameraName,
            @RequestParam(required = false, name = "noneGroup", defaultValue = "1") int noneGroup) throws ThingsboardException {
        try {
            SecurityUser securityUser = getCurrentUser();
            return new ResponseEntity<>(checkNotNull(cameraGroupService.getAllByGroupID(groupId, cameraName, noneGroup, securityUser)), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @ResponseBody
    @ApiOperation(value = "Get all camera group for select option filter", notes = "")
    @GetMapping("/filter")
    public ResponseEntity<?> getAllCameraGroupFilter() throws ThingsboardException {
        try {
            SecurityUser securityUser = getCurrentUser();
            return new ResponseEntity<>(checkNotNull(cameraGroupService.getListCameraGroupFilterDto(securityUser)), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
