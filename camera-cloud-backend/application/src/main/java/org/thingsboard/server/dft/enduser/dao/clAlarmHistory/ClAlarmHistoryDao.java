package org.thingsboard.server.dft.enduser.dao.clAlarmHistory;

import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dto.clAlarmHistory.ClAlarmHistoryDto;
import org.thingsboard.server.dft.enduser.dto.clAlarmHistory.ClAlarmHistoryInfoDto;

import java.util.Set;
import java.util.UUID;

public interface ClAlarmHistoryDao {
    ClAlarmHistoryDto save(ClAlarmHistoryDto clAlarmHistoryDto) throws ThingsboardException;

    PageData<ClAlarmHistoryInfoDto> getAllWithPaging(Pageable pageable, String textSearch, String type, UUID boxTbDeviceId, Boolean viewed, Long startTs, Long endTs, UUID tenantId);

    int countTotalNotViewAlarm(UUID tenantId);

    void markALarmViewed(Set<UUID> alarmHistoryIds, UUID tenantId);
}
