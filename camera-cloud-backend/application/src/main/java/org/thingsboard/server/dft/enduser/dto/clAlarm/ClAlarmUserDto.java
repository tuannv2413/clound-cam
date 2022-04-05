package org.thingsboard.server.dft.enduser.dto.clAlarm;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class ClAlarmUserDto {
    private UUID id; // tbUserId
    private String email;
}
