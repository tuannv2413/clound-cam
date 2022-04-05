package org.thingsboard.server.dft.enduser.dto.camera.settingAttribute;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.setting.BitrateSupport;
import org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.setting.FpsSupport;
import org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.setting.ResolutionSupport;

import java.util.List;

@Data
@NoArgsConstructor
public class CameraProfileSetting {
  private String channel;
  private List<ResolutionSupport> resolutionSupport;
  private BitrateSupport bitrateSupport;
  private FpsSupport fpsSupport;
}






