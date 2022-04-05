package org.thingsboard.server.dft.enduser.dto.camera.settingAttribute;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChannelRtspUrl {
  private String rtsp;

  public ChannelRtspUrl(String rtsp) {
    this.rtsp = rtsp;
  }
}
