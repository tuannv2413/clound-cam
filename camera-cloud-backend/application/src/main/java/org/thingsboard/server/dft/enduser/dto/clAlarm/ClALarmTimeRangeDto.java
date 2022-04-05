package org.thingsboard.server.dft.enduser.dto.clAlarm;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClALarmTimeRangeDto {
    private Long startTs; // milliseconds from 0h
    private Long endTs;
}
