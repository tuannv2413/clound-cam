package org.thingsboard.server.dft.enduser.service.clAlarmHistory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dao.clAlarmHistory.ClAlarmHistoryDao;
import org.thingsboard.server.dft.enduser.dto.clAlarmHistory.ClAlarmHistoryDto;
import org.thingsboard.server.dft.enduser.dto.clAlarmHistory.ClAlarmHistoryInfoDto;

import java.util.Set;
import java.util.UUID;

@Service
public class ClAlarmHistoryServiceImpl implements ClAlarmHistoryService {

    private final ClAlarmHistoryDao clAlarmHistoryDao;

    @Autowired
    public ClAlarmHistoryServiceImpl(ClAlarmHistoryDao clAlarmHistoryDao) {
        this.clAlarmHistoryDao = clAlarmHistoryDao;
    }

    @Override
    public ClAlarmHistoryDto save(ClAlarmHistoryDto clAlarmHistoryDto) throws ThingsboardException {
        return clAlarmHistoryDao.save(clAlarmHistoryDto);
    }

    @Override
    public PageData<ClAlarmHistoryInfoDto> getAllWithPaging(
            Pageable pageable,
            String textSearch,
            String type,
            UUID boxTbDeviceId,
            Boolean viewed,
            Long startTs,
            Long endTs,
            UUID tenantId) {
        return clAlarmHistoryDao.getAllWithPaging(pageable, textSearch, type, boxTbDeviceId, viewed, startTs, endTs, tenantId);
    }

    @Override
    public int countTotalNotViewAlarm(UUID tenantId) {
        return clAlarmHistoryDao.countTotalNotViewAlarm(tenantId);
    }

    @Override
    public void markALarmViewed(Set<UUID> alarmHistoryIds, UUID tenantId) {
        clAlarmHistoryDao.markALarmViewed(alarmHistoryIds, tenantId);
    }

}
