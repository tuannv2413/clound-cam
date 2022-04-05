package org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.setting;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConverResolutionSupport {
  private String displayKey;
  private ResolutionSupport settingValue;
}
