package org.thingsboard.server.dft.enduser.entity.camera.recordSetting;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel
@Data
@NoArgsConstructor
public class TimeRecordSetting {
  private long startTime;
  private long endTime;
}
