package org.thingsboard.server.dft.enduser.dto.clAlarm;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.thingsboard.server.common.data.device.profile.DeviceProfileAlarm;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EqualsAndHashCode
@Data
@NoArgsConstructor
public class ClAlarmReqDto {
    private UUID id;

    private UUID tenantId;

    @NotBlank(message = "alarmName can not be blank")
    private String alarmName;

    @NotBlank(message = "type can not be blank")
    private String type; // ClAlarmType

    private boolean viaNotify;
    private boolean viaSms;
    private boolean viaEmail;
    private boolean active;

    private Map<String, List<ClALarmTimeRangeDto>> timeAlarmSetting; // string: T2, T3, ...

    @NotNull(message = "boxDiviceIds can not be null")
    private List<UUID> boxDeviceIds; // list tbDeviceId of box

    @NotNull(message = "camDeviceIds can not be null")
    private List<UUID> camDeviceIds; // list tbDeviceId of cam

    @NotNull(message = "alarmReceivers can not be null")
    private List<UUID> alarmReceiverIds; // ng nhan cb

    private DeviceProfileAlarm dpAlarmForBox;
    private DeviceProfileAlarm dpAlarmForCam;
}