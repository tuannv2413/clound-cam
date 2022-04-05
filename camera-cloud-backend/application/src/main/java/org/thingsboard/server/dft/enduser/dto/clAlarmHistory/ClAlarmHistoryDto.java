package org.thingsboard.server.dft.enduser.dto.clAlarmHistory;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@EqualsAndHashCode
@Data
@NoArgsConstructor
public class ClAlarmHistoryDto {
    private UUID id;
    private UUID tenantId;
    private UUID tbAlarmId;
    private UUID tbDeviceId;
    private String deviceType; // box or cam (ClDeviceConstant)
    private String type;
    private boolean viewed;
    private Long createdTime;
}
