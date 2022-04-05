package org.thingsboard.server.dft.enduser.controller.auth;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.enduser.dto.auth.*;
import org.thingsboard.server.dft.enduser.dto.user.EndUserDto;
import org.thingsboard.server.dft.enduser.service.auth.AuthService;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.auth.rest.RestAuthenticationDetails;
import org.thingsboard.server.service.security.model.JwtTokenPair;
import org.thingsboard.server.service.security.model.SecurityUser;
import ua_parser.Client;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@RestController
@RequestMapping("/api")
@TbCoreComponent
@RequiredArgsConstructor
public class AuthEndUserController extends BaseController {

    private final AuthService authService;

    @ApiOperation("Api login for end-user")
    @PostMapping("/noauth/end-user/login")
    public ResponseEntity<JwtTokenPair> login(@RequestBody LoginRequest loginRequest) throws ThingsboardException {
        try {
            return ResponseEntity.ok(authService.login(loginRequest));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @ApiOperation(value = "End user Logout (logout)",
            notes = "Special API call to record the 'logout' of the end user to the Audit Logs. Since platform uses [JWT](https://jwt.io/), the actual logout is the procedure of clearing the [JWT](https://jwt.io/) token on the client side. ")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/auth/end-user/logout")
    @ResponseStatus(value = HttpStatus.OK)
    public void logout(HttpServletRequest request) throws ThingsboardException {
        logLogoutAction(request);
    }

    @ApiOperation(value = "End user refresh token")
    @PostMapping(value = "/noauth/end-user/refresh-token")
    public ResponseEntity<JwtTokenPair> refreshToken(@RequestBody RefreshTokenRequest request) throws ThingsboardException {
        try {
            return ResponseEntity.ok(authService.refreshToken(request));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    private void logLogoutAction(HttpServletRequest request) throws ThingsboardException {
        try {
            SecurityUser user = getCurrentUser();
            RestAuthenticationDetails details = new RestAuthenticationDetails(request);
            String clientAddress = details.getClientAddress();
            String browser = "Unknown";
            String os = "Unknown";
            String device = "Unknown";
            if (details.getUserAgent() != null) {
                Client userAgent = details.getUserAgent();
                if (userAgent.userAgent != null) {
                    browser = userAgent.userAgent.family;
                    if (userAgent.userAgent.major != null) {
                        browser += " " + userAgent.userAgent.major;
                        if (userAgent.userAgent.minor != null) {
                            browser += "." + userAgent.userAgent.minor;
                            if (userAgent.userAgent.patch != null) {
                                browser += "." + userAgent.userAgent.patch;
                            }
                        }
                    }
                }
                if (userAgent.os != null) {
                    os = userAgent.os.family;
                    if (userAgent.os.major != null) {
                        os += " " + userAgent.os.major;
                        if (userAgent.os.minor != null) {
                            os += "." + userAgent.os.minor;
                            if (userAgent.os.patch != null) {
                                os += "." + userAgent.os.patch;
                                if (userAgent.os.patchMinor != null) {
                                    os += "." + userAgent.os.patchMinor;
                                }
                            }
                        }
                    }
                }
                if (userAgent.device != null) {
                    device = userAgent.device.family;
                }
            }
            auditLogService.logEntityAction(
                    user.getTenantId(), user.getCustomerId(), user.getId(),
                    user.getName(), user.getId(), null, ActionType.LOGOUT, null, clientAddress, browser, os, device);

        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @ApiOperation("Api login for end-user")
    @GetMapping("/auth/end-user/me")
    public ResponseEntity<EndUserDto> getAccount() throws ThingsboardException {
        try {
            SecurityUser securityUser = getCurrentUser();
            return new ResponseEntity<>(checkNotNull(endUserService.getByUserId(securityUser.getUuidId(), securityUser)), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @ApiOperation("Api change password for end-user")
    @PostMapping("/auth/end-user/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) throws ThingsboardException {
        try {
            SecurityUser securityUser = getCurrentUser();
            return new ResponseEntity<>(endUserService.changePassword(request, securityUser), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @ApiOperation("Api send email to end-user when user forgot password")
    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping("/noauth/end-user/forgot-password/send-email")
    public void sendEmailForgotPassword(@RequestBody SendEmailForgotPasswordRequest sendEmailRequest,
                                        HttpServletRequest httpRequest) throws ThingsboardException {
        try {
            authService.sendEmailForgotPassword(sendEmailRequest, httpRequest);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @ApiOperation("Api check reset token when user forgot password after send link")
    @GetMapping("/noauth/end-user/checkResetToken")
    public ResponseEntity<String> checkResetToken(@RequestParam String resetToken, HttpServletRequest httpServletRequest) throws ThingsboardException {
        String resetURI = authService.getWebEndUserUrl(TenantId.SYS_TENANT_ID, httpServletRequest) + "auth/reset-password/";
        HttpHeaders headers = new HttpHeaders();
        HttpStatus responseStatus;
        try {
            authService.checkResetToken(resetToken);
            URI location = new URI(resetURI + resetToken);
            headers.setLocation(location);
            responseStatus = HttpStatus.SEE_OTHER;
        } catch (URISyntaxException e) {
            log.error("Unable to create URI with address [{}]", resetURI);
            responseStatus = HttpStatus.BAD_REQUEST;
        } catch (Exception e) {
            throw handleException(e);
        }
        return new ResponseEntity<>(headers, responseStatus);
    }

    @ApiOperation("Api reset password after check reset token success (forgot password)")
    @PostMapping("/noauth/end-user/resetPassword")
    public ResponseEntity<JwtTokenPair> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) throws ThingsboardException {
        try {
            return ResponseEntity.ok(authService.resetPassword(resetPasswordRequest));
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
