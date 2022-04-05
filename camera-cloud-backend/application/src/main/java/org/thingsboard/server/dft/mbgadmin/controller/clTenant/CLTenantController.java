package org.thingsboard.server.dft.mbgadmin.controller.clTenant;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.rule.engine.api.MailService;
import org.thingsboard.server.common.data.AdminSettings;
import org.thingsboard.server.common.data.Tenant;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.common.data.security.UserCredentials;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.exception.IncorrectParameterException;
import org.thingsboard.server.dao.settings.AdminSettingsService;
import org.thingsboard.server.dao.tenant.TenantService;
import org.thingsboard.server.dao.tenant.TenantServiceImpl;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseMessageDto;
import org.thingsboard.server.dft.mbgadmin.dto.clTenant.*;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseDataDto;
import org.thingsboard.server.dft.mbgadmin.dto.user.UserLoginDto;
import org.thingsboard.server.dft.mbgadmin.dto.user.UserMngDto;
import org.thingsboard.server.dft.mbgadmin.entity.clTenant.CLTenantEntity;
import org.thingsboard.server.dft.mbgadmin.entity.tenantQL.TenantEntityQL;
import org.thingsboard.server.dft.mbgadmin.service.clTenant.CLTenantService;
import org.thingsboard.server.dft.mbgadmin.service.user.UserRoleService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;
import org.thingsboard.server.service.security.system.SystemSecurityService;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@TbCoreComponent
@RequestMapping(value = "/api/mange/tenant")
public class CLTenantController extends BaseController {
    private static final String activated = "1";
    private static final String notActivated = "2";
    private static final String lockUp = "3";
    private static final String individual = "0"; // tư nhân
    private static final String enterprise = "1"; // doanh nghiệp
    public static final String TARGET_EMAIL = "targetEmail";

    private BCryptPasswordEncoder passwordEncoder;
    private CLTenantService clTenantService;
    protected UserRoleService userRoleService;
    private TenantService tenantService;
    private MailService mailService;
    private SystemSecurityService systemSecurityService;
    private final Configuration freemarkerConfig;
    private AdminSettingsService adminSettingsService;

    @Autowired
    public CLTenantController(BCryptPasswordEncoder passwordEncoder, CLTenantService clTenantService, UserRoleService userRoleService, TenantService tenantService, MailService mailService, SystemSecurityService systemSecurityService, Configuration freemarkerConfig, AdminSettingsService adminSettingsService) {
        this.passwordEncoder = passwordEncoder;
        this.clTenantService = clTenantService;
        this.userRoleService = userRoleService;
        this.tenantService = tenantService;
        this.mailService = mailService;
        this.systemSecurityService = systemSecurityService;
        this.freemarkerConfig = freemarkerConfig;
        this.adminSettingsService = adminSettingsService;
    }

    @PermitAll
    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getAll(
            @RequestParam(name = "page") Optional<Integer> page,
            @RequestParam(name = "pageSize") Optional<Integer> pageSize,
            @RequestParam(name = "typeCustomer", required = false, defaultValue = "") String typeCustomer,
            @RequestParam(name = "groupService", required = false) UUID groupService,
            @RequestParam(name = "state", required = false, defaultValue = "0") String state,
            @RequestParam(name = "startDate", required = false, defaultValue = "") String startDate,
            @RequestParam(name = "endDate", required = false, defaultValue = "") String endDate,
            @RequestParam(name = "textSearch", required = false, defaultValue = "") String textSearch,
            @RequestParam(name = "sortProperty", required = false, defaultValue = "created_time") String sortProperty,
            @RequestParam(name = "sortOrder", required = false, defaultValue = "desc") String sortOrder
    ) {
        // Convert Date
        long startDateConvert = 0;
        long endDateConvert = 0;
        try {
            startDateConvert = new SimpleDateFormat("dd/MM/yyyy").parse(startDate).getTime();
            endDateConvert = new SimpleDateFormat("dd/MM/yyyy").parse(endDate).getTime();
        } catch (Exception e) {
            startDateConvert = 0;
            endDateConvert = 0;
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Malformed Date. Format Date dd/MM/yyyy");
        }

        try {
            ResponseDataDto result = clTenantService.getPage(page.orElse(0), pageSize.orElse(10), typeCustomer.trim(), groupService, state, startDateConvert, endDateConvert, textSearch.trim(), sortProperty, sortOrder);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Get List Tenant Unsuccessful.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(49);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }
    }

    @PermitAll
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> create(@RequestBody CLTenantEditDto clTenantEditDto, HttpServletRequest request) throws ThingsboardException {
        try {
            new SimpleDateFormat("dd/MM/yyyy").parse(clTenantEditDto.getDayStartService()).getTime();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Malformed Date. Format Date dd/MM/yyyy");
        }

        // regex phone
        Boolean checkPhone = false;
//        String pattern = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$";
        String pattern = "^(\\\\+|[0-9])[0-9]{0,19}$";
        checkPhone = Pattern.matches(pattern, clTenantEditDto.getPhone());
        if (!checkPhone) {
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Số điện thoại không đúng định dạng.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(45);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }

        if (clTenantEditDto.getId() == null) {
            Boolean checkCode = clTenantService.checkCode(clTenantEditDto.getCode());
            if (checkCode) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Code Already Exist!!!");
            }
        }

        // check phone
        if (clTenantEditDto.getPhone() != null && userMngService.findUserByPhone(clTenantEditDto.getPhone().trim()) != null) {
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setStatus(406);
            responseMessageDto.setMessage("Số điện thoại đã tồn tại trên hệ thống.");
            responseMessageDto.setErrorCode(46);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }

        // Insert table tenant
        Tenant tenant = new Tenant();
        try {
            tenant.setCreatedTime(new Date().getTime());
            tenant.setEmail(clTenantEditDto.getEmail().trim());
            tenant.setPhone(clTenantEditDto.getPhone().trim());
            tenant.setState(notActivated);
            tenant.setAddress(clTenantEditDto.getAddress().trim());
            tenant.setTitle("Default");
            tenant.setTenantProfileId(null);
            tenant = tenantService.saveTenant(tenant);
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Insert Tenant Unsuccessful. " + e.getMessage());
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(47);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }

        // insert table tb_user
        User tbUser = new User();
        try {
            tbUser.setCreatedTime(new Date().getTime());
            tbUser.setEmail(clTenantEditDto.getEmail().trim());
            tbUser.setTenantId(tenant.getTenantId());
            tbUser.setAuthority(Authority.TENANT_ADMIN);
            tbUser.setFirstName(clTenantEditDto.getName().trim());
            tbUser.setLastName(clTenantEditDto.getName().trim());
            tbUser.setCustomerId(null);
            tbUser = userService.saveUser(tbUser);
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Insert TBUser Unsuccessful. " + e.getMessage());
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(48);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            tenantService.deleteTenant(tenant.getTenantId());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }

        // random password
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
        String password = RandomStringUtils.random(15, characters);

        //save user credentinals
        if (!StringUtils.isBlank(password)) {
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
            userCredentials.setPassword(passwordEncoder.encode(password));
            userService.replaceUserCredentials(tbUser.getTenantId(), userCredentials);
        }
        //end save user credentinals

        // insert table cl_user
        UserMngDto userDtoSave = new UserMngDto();
        try {
            UserMngDto userMngDto = new UserMngDto();
            userMngDto.setCreatedTime(System.currentTimeMillis());
            userMngDto.setTenantId(tenant.getId().getId());
            userMngDto.setCreatedBy(getCurrentUser().getUuidId());
            userMngDto.setUpdatedBy(getCurrentUser().getUuidId());
            userMngDto.setTbUserId(tbUser.getId().getId());
            userMngDto.setUpdatedTime(System.currentTimeMillis());
            userMngDto.setEmail(clTenantEditDto.getEmail().trim());
            userMngDto.setName(clTenantEditDto.getName().trim());
            userMngDto.setPhone(clTenantEditDto.getPhone().trim());
            userMngDto.setType(Authority.TENANT_ADMIN);
            userMngDto.setOffice(null);
            userMngDto.setSearchText(clTenantEditDto.getName().trim() + "; " + clTenantEditDto.getEmail().trim() + "; " + clTenantEditDto.getPhone().trim());
            userMngDto.setActive(true);
            userDtoSave = userMngService.createOrUpdateUserMng(userMngDto, passwordEncoder.encode(password));
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Insert User Unsuccessful. " + e.getMessage());
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(49);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            tenantService.deleteTenant(tenant.getTenantId());
            userService.deleteUser(tenant.getTenantId(), tbUser.getId());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }
        // insert table cl_tenant
        UUID currentUserId = getCurrentUser().getId().getId();
        CLTenantEditResponse result = clTenantService.createOrUpdate(clTenantEditDto, userDtoSave.getId(), currentUserId);

        if (result.getCode() == 52) {
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Insert Or Update CLTenantEntity Unsuccessful.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(52);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }
        if (result.getCode() == 53) {
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Insert Or Update MediaBoxEntity Unsuccessful.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(53);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }

        // Send mail username and password
        try {
            String email = userDtoSave.getEmail();
            UserCredentials userCredentials = userService.requestPasswordReset(TenantId.SYS_TENANT_ID, email);
            User user = userService.findUserById(TenantId.SYS_TENANT_ID, userCredentials.getUserId());
            String baseUrl = systemSecurityService.getBaseUrl(user.getTenantId(), user.getCustomerId(), request);
            String resetUrl = String.format("%s/api/noauth/end-user/checkResetToken?resetToken=%s", baseUrl,
                    userCredentials.getResetToken());

            // get link website
            SecurityUser securityUser = getCurrentUser();
            AdminSettings adminSettings = adminSettingsService.findAdminSettingsByKey(securityUser.getTenantId(), "web-enduser");
            String linkWebsite = adminSettings.getJsonValue().get("url").asText();

            String subject = "Đăng ký tài khoản Cloud Camera";
            Map<String, Object> model = new HashMap<>();
            model.put("linkEndUser", linkWebsite);
            model.put("name", userDtoSave.getName());
            model.put("email", email);
            model.put("phone", userDtoSave.getPhone());
            model.put("link", resetUrl);
            model.put(TARGET_EMAIL, email);

            String message = mergeTemplateIntoString("password.create.ftl", model);
            mailService.sendEmail(null, email, subject, message);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Gửi email thay đổi mật khẩu thất bại: " + e.getMessage());
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(51);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result.getData());
    }

    @PermitAll
    @PutMapping(value = "/{id}")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<?> update(@RequestBody CLTenantEditDto clTenantEditDto, @PathVariable("id") UUID id) throws ThingsboardException {
        try {
            new SimpleDateFormat("dd/MM/yyyy").parse(clTenantEditDto.getDayStartService()).getTime();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Malformed Date. Format Date dd/MM/yyyy");
        }

        clTenantEditDto.setId(id);
        // Check update hay insert
        GetIdDto getId = clTenantService.checkTenant(id);

        // regex phone
        Boolean checkPhone = false;
//        String pattern = "^(0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})$";
        String pattern = "^(\\\\+|[0-9])[0-9]{0,19}$";
        checkPhone = Pattern.matches(pattern, clTenantEditDto.getPhone());
        if (!checkPhone) {
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Số điện thoại không đúng định dạng.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(45);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }

        if (clTenantEditDto.getPhone() != null) {
            List<UserLoginDto> userByPhones = userMngService.findAllUserByPhone(clTenantEditDto.getPhone().trim());
            for (UserLoginDto userByPhone : userByPhones) {
                if (userByPhone.getId().compareTo(getId.getUserId()) != 0) {
                    ResponseMessageDto responseMessageDto = new ResponseMessageDto();
                    responseMessageDto.setStatus(406);
                    responseMessageDto.setMessage("Số điện thoại đã tồn tại trên hệ thống.");
                    responseMessageDto.setErrorCode(51);
                    responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
                }
            }
        }

        // Update table tenant
        Tenant tenant = tenantService.findTenantById(new TenantId(getId.getTenantId()));
        try {
            tenant.setId(new TenantId(getId.getTenantId()));
            tenant.setEmail(clTenantEditDto.getEmail().trim());
            tenant.setPhone(clTenantEditDto.getPhone().trim());
            tenant.setState(clTenantEditDto.getState() == 1 ? activated : clTenantEditDto.getState() == 2 ? notActivated : lockUp);
            tenant.setAddress(clTenantEditDto.getAddress());
            tenant = tenantService.saveTenant(tenant);
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Update Tenant Unsuccessful. " + e.getMessage());
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(47);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }

        // update table cl_user
        UserMngDto userDtoSave = new UserMngDto();
        try {
            UserMngDto userMngDto = userMngService.findUserByUserUUId(new UserId(getId.getUserId()));
            userMngDto.setId(getId.getUserId());
            userMngDto.setTenantId(tenant.getId().getId());
            userMngDto.setUpdatedBy(getCurrentUser().getUuidId());
            userMngDto.setUpdatedTime(System.currentTimeMillis());
            userMngDto.setName(clTenantEditDto.getName().trim());
            userMngDto.setPhone(clTenantEditDto.getPhone().trim());
            userMngDto.setType(Authority.TENANT_ADMIN);
            userMngDto.setSearchText(clTenantEditDto.getName() + "; " + clTenantEditDto.getEmail().trim() + "; " + clTenantEditDto.getPhone().trim());
            userDtoSave = userMngService.createOrUpdateUserMng(userMngDto, null);
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Update CLUser Unsuccessful. " + e.getMessage());
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(49);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }

        // update table tb_user
        try {
            User tbUser = userService.findUserById(new TenantId(tenant.getUuidId()), new UserId(userDtoSave.getTbUserId()));
            tbUser.setId(new UserId(getId.getTbUserId()));
            tbUser.setEmail(userDtoSave.getEmail());
            tbUser.setTenantId(tenant.getTenantId());
            tbUser.setAuthority(Authority.TENANT_ADMIN);
            tbUser.setFirstName(clTenantEditDto.getName().trim());
            tbUser.setLastName(clTenantEditDto.getName().trim());
            tbUser.setCustomerId(null);
            userService.saveUser(tbUser);
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Update TBUser Unsuccessful. " + e.getMessage());
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(48);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }

        UUID currentUserId = getCurrentUser().getId().getId();
        CLTenantEditResponse result = clTenantService.createOrUpdate(clTenantEditDto, userDtoSave.getId(), currentUserId);

        if (result.getCode() == 52) {
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Insert Or Update CLTenantEntity Unsuccessful.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(52);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }
        if (result.getCode() == 53) {
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Insert Or Update MediaBoxEntity Unsuccessful.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(53);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }

        return ResponseEntity.ok(result.getData());

    }

    @PermitAll
    @DeleteMapping(value = "/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteById(@PathVariable("id") UUID id) {
        try {
            CLTenantUpdateResponse result = clTenantService.deleteById(id, getCurrentUser().getId().getId());
            if (result.getCode() == 51 || result.getCode() == 50) {
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(result);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Delete Tenant By ID Unsuccessful.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(49);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }
    }

    @PermitAll
    @GetMapping(value = "/{id}")
    @ResponseBody
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
        try {
            return ResponseEntity.ok(clTenantService.getById(id));
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Get Tenant By ID Unsuccessful.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(49);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }
    }

    @PermitAll
    @GetMapping(value = "auto-generation-code")
    @ResponseBody
    public ResponseEntity<?> autoCodeGeneration() {
        try {
            return ResponseEntity.ok(clTenantService.autoCodeGeneration());
        } catch (Exception e) {
            handleException(e);
            ResponseMessageDto responseMessageDto = new ResponseMessageDto();
            responseMessageDto.setMessage("Gen Code Unsuccessful.");
            responseMessageDto.setStatus(417);
            responseMessageDto.setErrorCode(49);
            responseMessageDto.setTimestamp(new Timestamp(System.currentTimeMillis()));
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(responseMessageDto);
        }
    }

    private String mergeTemplateIntoString(String templateLocation,
                                           Map<String, Object> model) throws ThingsboardException {
        try {
            Template template = freemarkerConfig.getTemplate(templateLocation);
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
