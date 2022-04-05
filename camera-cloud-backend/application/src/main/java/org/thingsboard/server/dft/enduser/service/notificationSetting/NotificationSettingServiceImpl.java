package org.thingsboard.server.dft.enduser.service.notificationSetting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.AdminSettings;
import org.thingsboard.server.common.data.id.AdminSettingsId;
import org.thingsboard.server.dao.settings.AdminSettingsService;
import org.thingsboard.server.dft.enduser.dto.notificationSetting.NotificationSettingDto;
import org.thingsboard.server.dft.util.constant.NotificationSettingConstant;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Service
public class NotificationSettingServiceImpl implements NotificationSettingService {

    private NotificationSettingDto notificationSettingDto = null;

    private final AdminSettingsService adminSettingsService;
    private final ObjectMapper objectMapper;

    @Autowired
    public NotificationSettingServiceImpl(
            @Lazy AdminSettingsService adminSettingsService,
            ObjectMapper objectMapper
    ) {
        this.adminSettingsService = adminSettingsService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void initNotificationSetting() {
        AdminSettings adminSettings =
                adminSettingsService.findAdminSettingsByKey(null, NotificationSettingConstant.NOTIFY_KEY);
        try {
            notificationSettingDto =
                    objectMapper.treeToValue(adminSettings.getJsonValue(), NotificationSettingDto.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setNotificationSetting(NotificationSettingDto dto) {
        this.notificationSettingDto = dto;
    }

    @Override
    public NotificationSettingDto getNotificationSetting() {
        return notificationSettingDto;
    }

    @Override
    public NotificationSettingDto save(NotificationSettingDto dto) throws JsonProcessingException {
        AdminSettings adminSettings = adminSettingsService
                .findAdminSettingsByKey(null, NotificationSettingConstant.NOTIFY_KEY);
        JsonNode jsonNode = objectMapper.valueToTree(dto);
        if (adminSettings == null) {
            adminSettings = new AdminSettings();
            adminSettings.setId(new AdminSettingsId(UUID.randomUUID()));
            adminSettings.setCreatedTime(System.currentTimeMillis());
            adminSettings.setKey(NotificationSettingConstant.NOTIFY_KEY);

        }
        adminSettings.setJsonValue(jsonNode);
        AdminSettings savedAdminSetting = adminSettingsService.saveAdminSettings(null, adminSettings);
        NotificationSettingDto savedNotificationSetting = objectMapper.treeToValue(savedAdminSetting.getJsonValue(), NotificationSettingDto.class);
        setNotificationSetting(savedNotificationSetting);
        return savedNotificationSetting;
    }
}
