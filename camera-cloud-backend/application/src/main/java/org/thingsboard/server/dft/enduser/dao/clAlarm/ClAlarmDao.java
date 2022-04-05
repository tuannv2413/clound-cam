package org.thingsboard.server.dft.enduser.dao.clAlarm;

import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dto.clAlarm.ClAlarmInfoDto;
import org.thingsboard.server.dft.enduser.dto.clAlarm.ClAlarmReqDto;
import org.thingsboard.server.dft.enduser.dto.clAlarm.ClAlarmRespDto;
import org.thingsboard.server.dft.enduser.dto.clAlarm.ClAlarmTimeSettingDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.UUID;

public interface ClAlarmDao {

    PageData<ClAlarmInfoDto> getAllWithPaging(Pageable pageable, String textSearch, String type, UUID tenantId);

    ClAlarmRespDto getById(UUID id);

    ClAlarmRespDto save(ClAlarmReqDto clAlarmReqDto, String action, SecurityUser currentUser) throws ThingsboardException;

    void deleteById(UUID id);

    ClAlarmTimeSettingDto updateTimeAlarmSetting(ClAlarmTimeSettingDto dto);

}
