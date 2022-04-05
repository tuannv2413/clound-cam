package org.thingsboard.server.dft.enduser.service.auth;

import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.dft.enduser.dto.auth.LoginRequest;
import org.thingsboard.server.dft.enduser.dto.auth.RefreshTokenRequest;
import org.thingsboard.server.dft.enduser.dto.auth.ResetPasswordRequest;
import org.thingsboard.server.dft.enduser.dto.auth.SendEmailForgotPasswordRequest;
import org.thingsboard.server.service.security.model.JwtTokenPair;

import javax.servlet.http.HttpServletRequest;

public interface AuthService {

    JwtTokenPair login(LoginRequest loginRequest) throws ThingsboardException;

    JwtTokenPair refreshToken(RefreshTokenRequest request);

    JwtTokenPair resetPassword(ResetPasswordRequest resetPasswordRequest) throws ThingsboardException;

    void sendEmailForgotPassword(SendEmailForgotPasswordRequest sendEmailRequest, HttpServletRequest httpRequest) throws ThingsboardException;

    void checkResetToken(String resetToken);

    String getWebEndUserUrl(TenantId tenantId, HttpServletRequest httpServletRequest);
}
