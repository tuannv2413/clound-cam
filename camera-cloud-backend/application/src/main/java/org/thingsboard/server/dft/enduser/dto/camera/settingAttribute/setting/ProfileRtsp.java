package org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.setting;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProfileRtsp {
  private String channel;
  private String rtsp;

  public ProfileRtsp(String channel, String rtspUrl) {
    this.channel = channel;
    this.rtsp = rtspUrl;
  }
}
