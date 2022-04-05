package org.thingsboard.server.dft.enduser.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailForgotPasswordRequest {

    private String email;
}
