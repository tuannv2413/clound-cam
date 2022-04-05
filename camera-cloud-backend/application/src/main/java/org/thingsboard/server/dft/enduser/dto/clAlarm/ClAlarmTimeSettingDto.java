package org.thingsboard.server.dft.enduser.dto.clAlarm;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ClAlarmTimeSettingDto {

    @NotNull(value = "AlarmId can not be null")
    private UUID alarmId;

    private Map<String, List<ClALarmTimeRangeDto>> timeAlarmSetting; // string: T2, T3, ...
}
