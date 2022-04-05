package org.thingsboard.server.dft.mbgadmin.controller.user;

import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.common.data.security.UserCredentials;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.exception.IncorrectParameterException;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseMessageDto;
import org.thingsboard.server.dft.mbgadmin.dto.user.UserLoginDto;
import org.thingsboard.server.dft.mbgadmin.dto.user.UserMngCUDto;
import org.thingsboard.server.dft.mbgadmin.dto.user.UserMngDto;
import org.thingsboard.server.dft.mbgadmin.service.user.TBUserService;
import org.thingsboard.server.dft.mbgadmin.service.user.UserRoleService;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api/user-mng")
public class UserMngController extends BaseController {
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    protected UserRoleService userRoleService;

    @Autowired
    protected TBUserService tbUserService;

    @PermitAll
    @ResponseBody
    @ApiOperation(value = "Lấy 1 page user entity", notes = "")
    @GetMapping()
    public ResponseEntity<?> getAll(
            @RequestParam(name = "page") int page,
            @RequestParam(name = "pageSize") int pageSize,
            @RequestParam(name = "roleId", required = false) UUID roleId,
            @RequestParam(name = "active", required = false) Boolean active,
            @RequestParam(required = false, defaultValue = "") String textSearch,
            @RequestParam(required = false, defaultValue = "createdTime") String sortProperty,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        try {
            String sortField = sortProperty;
            if (sortProperty.equals("createdTime")) {
                sortField = "created_time";
            }
            if (sortProperty.equals("createdBy")) {
                sortField = "created_by";
            }
            if (sortProperty.equals("updatedBy")) {
                sortField = "updated_by";
            }
            Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortField);
            PageData<UserLoginDto> pageData = userMngService.getPage(roleId, active, pageable, textSearch);
            return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @PermitAll
    @DeleteMapping("{id}")
    @ResponseBody
    @ApiOperation(value = "Xóa user entity by id (UUID)", notes = "")
    public ResponseEntity<?> deleteById(@PathVariable("id") UUID id) {
        try {
            UserLoginDto user = userMngService.findUserByUserUUId(new UserId(id));
            if (user == null) {
                ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                responseMessageDto.setStatus(409);
                responseMessageDto.setMessage("Không tìm thấy thông tin cl_user cần xóa.");
                responseMessageDto.setErrorCode(55);
                responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseMessageDto);
            }

            if (user.getTbUserId() == null) {
                ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                responseMessageDto.setStatus(407);
                responseMessageDto.setMessage("Không tìm thấy thông tin tb_user cần xóa.");
                responseMessageDto.setErrorCode(52);
                responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseMessageDto);
            }

            if (StringUtils.isBlank(user.getEmail())) {
                ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                responseMessageDto.setStatus(408);
                responseMessageDto.setMessage("Thông tin email tài khoản cần xóa ko đúng do sai dữ liệu.");
                responseMessageDto.setErrorCode(54);
                responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseMessageDto);
            }

            User tbUser = new User();
            tbUser.setId(new UserId((user.getTbUserId())));
            tbUser.setEmail(user.getTbUserId() + "-" + user.getEmail());
            tbUserService.changeUserEmail(tbUser);

            boolean deletedStatus = userMngService.deleteById(user.getTenantId(), id, getCurrentUser().getUuidId());
            return new ResponseEntity<>(deletedStatus, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @PermitAll
    @PostMapping()
    @ResponseBody
    @ApiOperation(value = "Tạo/thêm mới user entity", notes = "")
    public ResponseEntity<?> createUser(@RequestBody UserMngCUDto user, HttpServletRequest request) throws ThingsboardException {
        try {
            if (user.getPhone() != null) {
                List<UserLoginDto> userByPhones = userMngService.findAllUserByPhone(user.getPhone().trim());
                if (userByPhones != null && !userByPhones.isEmpty()) {
                    ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                    responseMessageDto.setStatus(406);
                    responseMessageDto.setMessage("Số điện thoại đã tồn tại trên hệ thống.");
                    responseMessageDto.setErrorCode(51);
                    responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
                }
            }

            User tbUser = new User();
            tbUser.setCreatedTime(System.currentTimeMillis());
            tbUser.setEmail(user.getEmail());
            tbUser.setTenantId(getTenantId());
            tbUser.setAuthority(Authority.SYS_ADMIN);
            tbUser.setFirstName(user.getName());
            tbUser.setLastName(user.getName());
            tbUser.setCustomerId(getCurrentUser().getCustomerId());

            //tbUser.setSearchText(user.getFullName() + "; " + user.getEmail() + "; " + user.getPhone() + "; " + user.getOffice());
            tbUser = userService.saveUser(tbUser);

            //save user credentinals
            if (!StringUtils.isBlank(user.getPassword())) {
                UserCredentials userCredentials = userService.findUserCredentialsByUserId(tbUser.getTenantId(), tbUser.getId());
                if (userCredentials == null) {
                    userCredentials = new UserCredentials();
                    userCredentials.setEnabled(false);
                    userCredentials.setUserId(tbUser.getId());
                }
                if (userCredentials.isEnabled()) {
                    throw new IncorrectParameterException("User credentials already activated");
                }
                userCredentials.setEnabled(true);
                userCredentials.setActivateToken(null);
                userCredentials.setPassword(passwordEncoder.encode(user.getPassword()));
                userService.replaceUserCredentials(tbUser.getTenantId(), userCredentials);
            }
            //end save user credentinals

            userRoleService.addUserRoles(tbUser.getId().getId(), new UUID[]{user.getRoles()});

            UserMngDto userMngDto = new UserMngDto();
            userMngDto.setTenantId(getTenantId().getId());
            userMngDto.setCreatedBy(getCurrentUser().getUuidId());
            userMngDto.setUpdatedBy(getCurrentUser().getUuidId());
            userMngDto.setTbUserId(tbUser.getId().getId());
            userMngDto.setCreatedTime(System.currentTimeMillis());
            userMngDto.setUpdatedTime(System.currentTimeMillis());
            userMngDto.setEmail(user.getEmail());
            userMngDto.setName(user.getName());
            userMngDto.setPhone(user.getPhone());
            userMngDto.setType(Authority.SYS_ADMIN);
            userMngDto.setOffice(user.getOffice());
            userMngDto.setSearchText(user.getName() + "; " + user.getEmail() + "; " + user.getPhone() + "; " + user.getOffice());
            userMngDto.setActive(true);

            UserMngDto userDtoSave = userMngService.createOrUpdateUserMng(userMngDto, user.getPassword());

            UserLoginDto res = userMngService.findUserByUserId(new UserId(userDtoSave.getTbUserId()));
            return new ResponseEntity<>(checkNotNull(res), HttpStatus.OK);
        } catch (Exception e) {
            if (e.getMessage().equals("User with email '" + user.getEmail().toLowerCase() + "'  already present in database!")) {
                ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                responseMessageDto.setStatus(403);
                responseMessageDto.setMessage("Email đã tồn tại trên hệ thống.");
                responseMessageDto.setErrorCode(49);
                responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
            }
            if (e.getMessage().contains("Invalid email address format")) {
                ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                responseMessageDto.setStatus(405);
                responseMessageDto.setMessage("Sai định dạng email.");
                responseMessageDto.setErrorCode(50);
                responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
            }
            throw handleException(e);
        }
    }

    @PostMapping(value = "/upload-files/{id}")
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file, @PathVariable("id") UUID id) {
        try {
            if (file.getSize() > 5242880) {
                ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                responseMessageDto.setStatus(450);
                responseMessageDto.setMessage("Dung lượng ảnh tối đa 5MB.");
                responseMessageDto.setErrorCode(56);
                responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
            }
            UserLoginDto user = userMngService.findUserByUserUUId(new UserId(id));
            if (user == null) {
                ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                responseMessageDto.setStatus(406);
                responseMessageDto.setMessage("Không tìm thấy thông tin user cần cập nhật avatar.");
                responseMessageDto.setErrorCode(51);
                responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
            }
            UserMngDto userAvatar = userMngService.upload(file, id);
            return new ResponseEntity<>(checkNotNull(userAvatar), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @PermitAll
    @PutMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "sửa user entity", notes = "")
    public ResponseEntity<?> editUser(@PathVariable("id") UUID id, @RequestBody UserMngCUDto user, HttpServletRequest request) {
        try {
            if (user.getPhone() != null) {
                List<UserLoginDto> userByPhones = userMngService.findAllUserByPhone(user.getPhone().trim());
                for (UserLoginDto userByPhone : userByPhones) {
                    if (userByPhone.getId().compareTo(id) != 0) {
                        ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                        responseMessageDto.setStatus(406);
                        responseMessageDto.setMessage("Số điện thoại đã tồn tại trên hệ thống.");
                        responseMessageDto.setErrorCode(51);
                        responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
                    }
                }
            }

            UserMngDto userMngDto = userMngService.findUserByUserUUId(new UserId(id));
            if (userMngDto != null) {
                userMngDto.setId(id);
                userMngDto.setTenantId(getTenantId().getId());
                userMngDto.setUpdatedBy(getCurrentUser().getUuidId());
                userMngDto.setUpdatedTime(System.currentTimeMillis());
                userMngDto.setName(user.getName());
                userMngDto.setPhone(user.getPhone());
                userMngDto.setType(Authority.SYS_ADMIN);
                userMngDto.setOffice(user.getOffice());
                userMngDto.setActive(user.isActive());
                userMngDto.setSearchText(user.getName() + "; " + userMngDto.getEmail() + "; " + user.getPhone() + "; " + user.getOffice());

                UserMngDto userDtoSave = userMngService.createOrUpdateUserMng(userMngDto, null);

                User tbUser = userService.findUserById(new TenantId(userDtoSave.getTenantId()), new UserId(userDtoSave.getTbUserId()));
                tbUser.setId(new UserId((userDtoSave.getTbUserId())));
                tbUser.setEmail(userDtoSave.getEmail());
                tbUser.setTenantId(getTenantId());
                tbUser.setAuthority(Authority.SYS_ADMIN);
                tbUser.setFirstName(user.getName());
                tbUser.setLastName(user.getName());
                tbUser.setCustomerId(getCurrentUser().getCustomerId());
                userService.saveUser(tbUser);

                userRoleService.updateTbUserRoles(userDtoSave.getTbUserId(), new UUID[]{user.getRoles()});

                UserLoginDto res = userMngService.findUserByUserId(new UserId(userDtoSave.getTbUserId()));
                return new ResponseEntity<>(checkNotNull(res), HttpStatus.OK);
            } else {
                ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                responseMessageDto.setStatus(406);
                responseMessageDto.setMessage("Không tìm thấy thông tin user cần sửa.");
                responseMessageDto.setErrorCode(51);
                responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    @PermitAll
    @GetMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "lấy user entity", notes = "")
    public ResponseEntity<?> findByUserId(@PathVariable("id") UUID id, HttpServletRequest request) {
        try {
            UserLoginDto res = userMngService.findUserByUserUUId(new UserId(id));
            return new ResponseEntity<>(checkNotNull(res), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }
}
