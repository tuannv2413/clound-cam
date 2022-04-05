package org.thingsboard.server.dft.enduser.dto.clAlarm;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.thingsboard.server.common.data.Device;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode
@Data
@NoArgsConstructor
public class ClAlarmRespDto {
    private UUID id;
    private UUID tenantId;
    private String alarmName;
    private String type; // ClAlarmType : CONNECT, DISCONNECT, MOVING
    private boolean viaNotify;
    private boolean viaSms;
    private boolean viaEmail;
    private boolean active;
    private Map<String, List<ClALarmTimeRangeDto>> timeAlarmSetting;
    private List<Device> devices; // list devices related to this alarm
    private List<UUID> boxDeviceIds;
    private List<UUID> camDeviceIds;
    private List<ClAlarmUserDto> alarmReceivers;
    private List<UUID> alarmReceiverIds;
}
