package org.thingsboard.server.dft.enduser.service.mail;

import org.thingsboard.server.common.data.exception.ThingsboardException;

public interface MailService {

    void updateMailConfiguration();

    void sendResetPasswordEmail(String passwordResetLink, String email, String fullName) throws ThingsboardException;

    void sendResetPasswordEmailAsync(String passwordResetLink, String email, String fullName);
}
