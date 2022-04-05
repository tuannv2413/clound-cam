package org.thingsboard.server.dft.enduser.dto.notificationSetting;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class NotificationSettingDto {

    @NotNull(message = "firebaseApiUrl is mandatory")
    private String firebaseApiUrl;

    @NotNull(message = "firebaseAccessToken is mandatory")
    private String firebaseAccessToken;
}
