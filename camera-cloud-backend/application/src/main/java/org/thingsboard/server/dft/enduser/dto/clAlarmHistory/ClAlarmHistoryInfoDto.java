package org.thingsboard.server.dft.enduser.dto.clAlarmHistory;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class ClAlarmHistoryInfoDto {
    private UUID id;
    private UUID tenantId;
    private UUID tbAlarmId;
    private UUID tbDeviceId;
    private String deviceName; // name of box or cam ( cl_cam or cl_box )
    private String deviceType; // box or cam
    private String boxName; // null when device is box
    private String type; // chdong, knoi, matknoi
    private boolean viewed;
    private Long createdTime;
}