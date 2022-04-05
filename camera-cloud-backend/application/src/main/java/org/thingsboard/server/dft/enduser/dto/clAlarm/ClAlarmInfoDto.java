package org.thingsboard.server.dft.enduser.dto.clAlarm;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.dft.enduser.dto.BaseInfoDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
public class ClAlarmInfoDto extends BaseInfoDto {
    private UUID id;
    private UUID tenantId;
    private String alarmName;
    private String type; // ClAlarmType
    private boolean viaNotify;
    private boolean viaSms;
    private boolean viaEmail;
    private boolean active;
    private Map<String, List<ClALarmTimeRangeDto>> timeAlarmSetting;
    private List<Device> devices;
    private List<ClAlarmUserDto> alarmReceivers;
    private Long numberOfCam;
    private Long numberOfBox;
    private String createdByStr;
    private String updatedByStr;
}
