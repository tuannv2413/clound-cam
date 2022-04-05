package org.thingsboard.server.dft.enduser.dto.camera;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.setting.ConverResolutionSupport;

import java.util.List;

@Data
@NoArgsConstructor
public class CameraAdvanceSettingOption {
  private List<ConverResolutionSupport> resolutionSupport;
  private List<Integer> fpsSupport;
  private List<Long> bitrateSupport;
}
