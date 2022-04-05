package org.thingsboard.server.dft.mbgadmin.controller.role;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseMessageDto;
import org.thingsboard.server.dft.mbgadmin.dto.role.RoleDto;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import javax.annotation.security.PermitAll;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api/role")
public class RoleController extends BaseController {
    @PermitAll
    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getAll(
            @RequestParam(name = "pageSize") int pageSize,
            @RequestParam(name = "page") int page,
            @RequestParam(required = false, defaultValue = "") String textSearch,
            @RequestParam(required = false, defaultValue = "createdTime") String sortProperty,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        try {
            Pageable pageable =
                    PageRequest.of(page, pageSize);
            PageData<RoleDto> pageData =
                    roleService.getPage(pageable, textSearch.trim(), sortProperty, sortOrder);
            return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @PermitAll
    @GetMapping(value="/all")
    @ResponseBody
    public ResponseEntity<?> getAllRoles() {
        try {
            List<RoleDto> roleDtoList = roleService.findAll();
            return new ResponseEntity<>(checkNotNull(roleDtoList), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }


    @PermitAll
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createOrUpdate(@RequestBody RoleDto roleDto) {
        try {
            SecurityUser securityUser = getCurrentUser();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            if(roleService.existsRoleEntityByRoleName(roleDto.getRoleName())){
                RoleDto roleResponse = roleService.findRoleEntityByRoleName(roleDto.getRoleName());
                if(!roleResponse.getId().equals(roleDto.getId()) && roleResponse.getRoleName().equals(roleDto.getRoleName())){
                    ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                    responseMessageDto.setStatus(403);
                    responseMessageDto.setMessage("Tên nhóm người dùng đã tồn tại");
                    responseMessageDto.setErrorCode(49);
                    responseMessageDto.setTimestamp(timestamp);
                    return new ResponseEntity<>(responseMessageDto, HttpStatus.FORBIDDEN);
                }
            }
            if(roleDto.getRoleName().isEmpty() || roleDto.getRoleName() == "" || roleDto.getRoleName() == null){
                ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                responseMessageDto.setStatus(403);
                responseMessageDto.setMessage("Tên nhóm người dùng không được để trống");
                responseMessageDto.setErrorCode(47);
                responseMessageDto.setTimestamp(timestamp);
                return new ResponseEntity<>(responseMessageDto, HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(checkNotNull(roleService.createOrUpdate(roleDto, securityUser)), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @PermitAll
    @GetMapping(value="{id}")
    @ResponseBody
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
        try {
            return new ResponseEntity<>(checkNotNull(roleService.getById(id)), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @PermitAll
    @DeleteMapping("{id}")
    @ResponseBody
    public ResponseEntity<?> deleteById(@PathVariable("id") UUID id) {
        try {
            if(userMngService.findUsersCountByRoleId(id) > 0){
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("Đã có người dùng tồn tại trong nhóm.");
            }else {
                roleService.deleteById(id);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }
}
