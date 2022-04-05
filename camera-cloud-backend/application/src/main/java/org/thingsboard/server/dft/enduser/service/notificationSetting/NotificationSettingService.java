package org.thingsboard.server.dft.enduser.service.notificationSetting;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.thingsboard.server.dft.enduser.dto.notificationSetting.NotificationSettingDto;

public interface NotificationSettingService {

    NotificationSettingDto getNotificationSetting();

    NotificationSettingDto save(NotificationSettingDto notificationSettingDto) throws JsonProcessingException;
}
