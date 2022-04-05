package org.thingsboard.server.dft.enduser.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    private String resetToken;

    private String password;
}
