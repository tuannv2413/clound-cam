package org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.setting;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BitrateSupport {
  private long max;
  private long min;
}