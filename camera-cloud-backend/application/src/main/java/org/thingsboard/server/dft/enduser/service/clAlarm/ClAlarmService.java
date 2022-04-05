package org.thingsboard.server.dft.enduser.service.clAlarm;

import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dto.clAlarm.*;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;

public interface ClAlarmService {

    PageData<ClAlarmInfoDto> getAllWithPaging(Pageable pageable, String textSearch, String type, UUID tenantId);

    ClAlarmRespDto getById(UUID id);

    ClAlarmRespDto save(ClAlarmReqDto clAlarmReqDto, String action, SecurityUser currentUser) throws ThingsboardException;

    void deleteById(UUID id);

    ClAlarmTimeSettingDto updateTimeAlarmSetting(ClAlarmTimeSettingDto dto);

    void sendWarningAsync(UUID tenantId, Long alarmTime,  UUID tbDeviceId, String deviceType, ClAlarmRespDto clAlarmRespDto);
}
