package org.thingsboard.server.dft.enduser.dto.clAlarmHistory;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ClAlarmHistoryMarkViewedDto {
    private Set<UUID> alarmHistoryIds;
}
