package org.thingsboard.server.dft.enduser.service.auth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.AdminSettings;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.security.UserCredentials;
import org.thingsboard.server.common.data.security.model.JwtToken;
import org.thingsboard.server.dao.exception.DataValidationException;
import org.thingsboard.server.dao.exception.IncorrectParameterException;
import org.thingsboard.server.dao.model.sql.UserCredentialsEntity;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dao.service.DataValidator;
import org.thingsboard.server.dao.settings.AdminSettingsService;
import org.thingsboard.server.dao.user.UserCredentialsDao;
import org.thingsboard.server.dao.user.UserService;
import org.thingsboard.server.dft.enduser.dao.user.EndUserDao;
import org.thingsboard.server.dft.enduser.dao.user.UserDao;
import org.thingsboard.server.dft.enduser.dto.auth.LoginRequest;
import org.thingsboard.server.dft.enduser.dto.auth.RefreshTokenRequest;
import org.thingsboard.server.dft.enduser.dto.auth.ResetPasswordRequest;
import org.thingsboard.server.dft.enduser.dto.auth.SendEmailForgotPasswordRequest;
import org.thingsboard.server.dft.enduser.dto.user.EndUserDto;
import org.thingsboard.server.dft.enduser.entity.user.EndUserEntity;
import org.thingsboard.server.dft.enduser.repository.user.EndUserRepository;
import org.thingsboard.server.dft.enduser.service.mail.MailService;
import org.thingsboard.server.dft.util.constant.EndUserConstant;
import org.thingsboard.server.service.security.model.JwtTokenPair;
import org.thingsboard.server.service.security.model.SecurityUser;
import org.thingsboard.server.service.security.model.UserPrincipal;
import org.thingsboard.server.service.security.model.token.JwtTokenFactory;
import org.thingsboard.server.service.security.model.token.RawAccessJwtToken;
import org.thingsboard.server.service.security.system.SystemSecurityService;
import org.thingsboard.server.utils.MiscUtils;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices.DEFAULT_TOKEN_LENGTH;
import static org.thingsboard.server.dao.user.UserServiceImpl.USER_PASSWORD_HISTORY;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final EndUserDao endUserDao;

    private final UserDao userDao;

    private final UserCredentialsDao userCredentialsDao;

    private final JwtTokenFactory tokenFactory;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final SystemSecurityService systemSecurityService;

    private final MailService mailService;

    private final EndUserRepository endUserRepository;

    private final AdminSettingsService adminSettingsService;

    @Value("${security.user_login_case_sensitive:true}")
    private boolean userLoginCaseSensitive;

    @Transactional
    @Override
    public JwtTokenPair login(LoginRequest loginRequest) throws ThingsboardException {
        String username = loginRequest.getUsername().trim();
        String password = loginRequest.getPassword().trim();
        UserEntity userEntity = null;
        if (username.contains("@")) {
            userEntity = userDao.findByEmail(username);
            if (userEntity != null) {
                EndUserDto endUserEntity = endUserRepository.findByUserId(userEntity.getId()).map(EndUserDto::new).orElse(null);
                if (endUserEntity != null && Boolean.FALSE.equals(endUserEntity.isActive())) {
                    throw new ThingsboardException("Tài khoản hiện tại đang bị khóa!", ThingsboardErrorCode.AUTHENTICATION);
                } else if (endUserEntity != null && Boolean.TRUE.equals(endUserEntity.isDelete())) {
                    throw new ThingsboardException("Tài khoản đăng nhập hoặc mật khẩu sai!", ThingsboardErrorCode.AUTHENTICATION);
                }
            }
        } else {
            EndUserEntity endUserEntity = endUserDao.findByPhone(null, username);
            if (endUserEntity != null) {
                if (Boolean.FALSE.equals(endUserEntity.isActive())) {
                    throw new ThingsboardException("Tài khoản hiện tại đang bị khóa!", ThingsboardErrorCode.AUTHENTICATION);
                } else if (Boolean.TRUE.equals(endUserEntity.isDelete())) {
                    throw new ThingsboardException("Tài khoản đăng nhập hoặc mật khẩu sai!", ThingsboardErrorCode.AUTHENTICATION);
                }
                userEntity = userDao.findByEndUserId(endUserEntity.getTenantId(), endUserEntity.getId());
            }
        }
        if (userEntity != null) {
            UserCredentials userCredentials = userCredentialsDao.findByUserId(new TenantId(userEntity.getTenantId()), userEntity.getId());
            if (userCredentials != null && Boolean.TRUE.equals(userCredentials.isEnabled()) && passwordEncoder.matches(password, userCredentials.getPassword())) {
                User user = userService.findUserById(TenantId.SYS_TENANT_ID, userCredentials.getUserId());
                return getJwtTokenPair(user, userCredentials.isEnabled());
            }
        }
        throw new ThingsboardException("Tài khoản đăng nhập hoặc mật khẩu sai!", ThingsboardErrorCode.AUTHENTICATION);
    }

    @Override
    public JwtTokenPair refreshToken(RefreshTokenRequest request) {
        RawAccessJwtToken requestRefreshToken = new RawAccessJwtToken(request.getRefreshToken());
        SecurityUser userCredentials = tokenFactory.parseRefreshToken(requestRefreshToken);
        if (userCredentials != null) {
            User user = userService.findUserById(TenantId.SYS_TENANT_ID, userCredentials.getId());
            return getJwtTokenPair(user, userCredentials.isEnabled());
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, EndUserConstant.UNAUTHORIZED);
    }

    @Override
    public JwtTokenPair resetPassword(ResetPasswordRequest resetPasswordRequest) throws ThingsboardException {
        String resetToken = resetPasswordRequest.getResetToken();
        String password = resetPasswordRequest.getPassword();
        UserCredentials userCredentials = userService.findUserCredentialsByResetToken(TenantId.SYS_TENANT_ID, resetToken);
        if (userCredentials != null) {
            systemSecurityService.validatePassword(TenantId.SYS_TENANT_ID, password, userCredentials);
            if (passwordEncoder.matches(password, userCredentials.getPassword())) {
                throw new ThingsboardException("Mật khẩu mới không được trùng với mật khẩu cũ!", ThingsboardErrorCode.NEWPASSWORD_SAME_OLDPASSWROD);
            }
            String encodedPassword = passwordEncoder.encode(password);
            userCredentials.setPassword(encodedPassword);
            userCredentials.setResetToken(null);
            UserCredentialsEntity userCredentialsEntity = saveUserCredentials(TenantId.SYS_TENANT_ID, userCredentials);
            User user = userService.findUserById(TenantId.SYS_TENANT_ID, new UserId(userCredentialsEntity.getUserId()));

            return getJwtTokenPair(user, userCredentialsEntity.isEnabled());
        } else {
            throw new ThingsboardException("Invalid reset token!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }
    }

    @Override
    public void sendEmailForgotPassword(SendEmailForgotPasswordRequest sendEmailRequest, HttpServletRequest httpRequest) throws ThingsboardException {
        UserCredentialsEntity userCredentials = requestPasswordReset(TenantId.SYS_TENANT_ID, sendEmailRequest.getEmail());
        EndUserDto endUserDto = endUserDao.getByUserId(userCredentials.getUserId());
        if (Boolean.FALSE.equals(endUserDto.isActive())) {
            throw new ThingsboardException("Tài khoản của bạn đã bị khóa. Không thể tạo mật khẩu", ThingsboardErrorCode.AUTHENTICATION);
        }
        if (Boolean.TRUE.equals(endUserDto.isDelete())) {
            throw new UsernameNotFoundException("Email không tồn tại!");
        }
        String baseUrl = systemSecurityService.getBaseUrl(new TenantId(endUserDto.getTenantId()), new CustomerId(endUserDto.getCustomerId()), httpRequest);
        String resetUrl = String.format("%s/api/noauth/end-user/checkResetToken?resetToken=%s", baseUrl, userCredentials.getResetToken());
        String fullName = endUserDto.getName();
        if (fullName == null || StringUtils.isBlank(fullName)) {
            fullName = "";
            User user = userService.findUserById(TenantId.SYS_TENANT_ID, new UserId(userCredentials.getUserId()));
            if (user.getLastName() != null && !StringUtils.isBlank(user.getLastName())) {
                fullName += user.getLastName() + " ";
            }
            if (user.getFirstName() != null && !StringUtils.isBlank(user.getFirstName())) {
                fullName += user.getFirstName();
            }
            if (StringUtils.isBlank(fullName) && user.getEmail() != null) {
                fullName = user.getEmail();
            }
        }
        mailService.sendResetPasswordEmailAsync(resetUrl, sendEmailRequest.getEmail(), fullName.trim());
    }

    @Override
    public void checkResetToken(String resetToken) {
        UserCredentials userCredentials = userService.findUserCredentialsByResetToken(TenantId.SYS_TENANT_ID, resetToken);
        if (userCredentials == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset token invalid!");
        }
    }

    @Override
    public String getWebEndUserUrl(TenantId tenantId, HttpServletRequest httpServletRequest) {
        String baseUrl = null;
        AdminSettings webEndUserSetting = adminSettingsService.findAdminSettingsByKey(tenantId, "web-end-user");

        JsonNode prohibitDifferentUrl = webEndUserSetting.getJsonValue().get("url");

        if (prohibitDifferentUrl != null) {
            baseUrl = webEndUserSetting.getJsonValue().get("url").asText();
        }

        if (StringUtils.isEmpty(baseUrl)) {
            baseUrl = MiscUtils.constructBaseUrl(httpServletRequest);
        }

        return baseUrl;
    }

    public UserCredentialsEntity requestPasswordReset(TenantId tenantId, String email) {
        log.trace("Executing end-user requestPasswordReset email [{}]", email);
        DataValidator.validateEmail(email);
        User user = userService.findUserByEmail(tenantId, email);
        if (user == null) {
            throw new UsernameNotFoundException("Email không tồn tại!");
        }
        UserCredentials userCredentials = userCredentialsDao.findByUserId(tenantId, user.getUuidId());
        userCredentials.setResetToken(RandomStringUtils.randomAlphanumeric(DEFAULT_TOKEN_LENGTH));
        return saveUserCredentials(tenantId, userCredentials);
    }

    public UserCredentialsEntity saveUserCredentials(TenantId tenantId, UserCredentials userCredentials) {
        log.trace("Executing saveUserCredentials [{}]", userCredentials);
        userCredentialsValidator.validate(userCredentials, data -> tenantId);
        return saveUserCredentialsAndPasswordHistory(tenantId, userCredentials);
    }

    private UserCredentialsEntity saveUserCredentialsAndPasswordHistory(TenantId tenantId, UserCredentials userCredentials) {
        UserCredentialsEntity result = userCredentialsDao.save(userCredentials);
        User user = userService.findUserById(tenantId, userCredentials.getUserId());
        if (userCredentials.getPassword() != null) {
            updatePasswordHistory(user, userCredentials);
        }
        return result;
    }

    private void updatePasswordHistory(User user, UserCredentials userCredentials) {
        JsonNode additionalInfo = user.getAdditionalInfo();
        if (!(additionalInfo instanceof ObjectNode)) {
            additionalInfo = JacksonUtil.newObjectNode();
        }
        Map<String, String> userPasswordHistoryMap = null;
        JsonNode userPasswordHistoryJson;
        if (additionalInfo.has(USER_PASSWORD_HISTORY)) {
            userPasswordHistoryJson = additionalInfo.get(USER_PASSWORD_HISTORY);
            userPasswordHistoryMap = JacksonUtil.convertValue(userPasswordHistoryJson, new TypeReference<>() {
            });
        }
        if (userPasswordHistoryMap != null) {
            userPasswordHistoryMap.put(Long.toString(System.currentTimeMillis()), userCredentials.getPassword());
            userPasswordHistoryJson = JacksonUtil.valueToTree(userPasswordHistoryMap);
            ((ObjectNode) additionalInfo).replace(USER_PASSWORD_HISTORY, userPasswordHistoryJson);
        } else {
            userPasswordHistoryMap = new HashMap<>();
            userPasswordHistoryMap.put(Long.toString(System.currentTimeMillis()), userCredentials.getPassword());
            userPasswordHistoryJson = JacksonUtil.valueToTree(userPasswordHistoryMap);
            ((ObjectNode) additionalInfo).set(USER_PASSWORD_HISTORY, userPasswordHistoryJson);
        }
        user.setAdditionalInfo(additionalInfo);
        saveUser(user);
    }

    public void saveUser(User user) {
        log.trace("Executing saveUser [{}]", user);
        if (!userLoginCaseSensitive) {
            user.setEmail(user.getEmail().toLowerCase());
        }
        userDao.save(user);
    }

    private final DataValidator<UserCredentials> userCredentialsValidator = new DataValidator<>() {

        @Override
        protected void validateCreate(TenantId tenantId, UserCredentials userCredentials) {
            throw new IncorrectParameterException("Creation of new user credentials is prohibited.");
        }

        @Override
        protected void validateDataImpl(TenantId tenantId, UserCredentials userCredentials) {
            if (userCredentials.getUserId() == null) {
                throw new DataValidationException("User credentials should be assigned to user!");
            }
            if (userCredentials.isEnabled()) {
                if (StringUtils.isEmpty(userCredentials.getPassword())) {
                    throw new DataValidationException("Enabled user credentials should have password!");
                }
                if (StringUtils.isNotEmpty(userCredentials.getActivateToken())) {
                    throw new DataValidationException("Enabled user credentials can't have activate token!");
                }
            }
            UserCredentials existingUserCredentialsEntity = userCredentialsDao.findById(tenantId, userCredentials.getId().getId());
            if (existingUserCredentialsEntity == null) {
                throw new DataValidationException("Unable to update non-existent user credentials!");
            }
            User user = userService.findUserById(tenantId, userCredentials.getUserId());
            if (user == null) {
                throw new DataValidationException("Can't assign user credentials to non-existent user!");
            }
        }
    };

    @NotNull
    private JwtTokenPair getJwtTokenPair(User user, boolean enabled) {
        UserPrincipal principal = new UserPrincipal(UserPrincipal.Type.USER_NAME, user.getEmail());
        SecurityUser securityUser = new SecurityUser(user, enabled, principal);
        JwtToken accessToken = tokenFactory.createAccessJwtToken(securityUser);
        JwtToken refreshToken = tokenFactory.createRefreshToken(securityUser);
        return new JwtTokenPair(accessToken.getToken(), refreshToken.getToken());
    }
}
