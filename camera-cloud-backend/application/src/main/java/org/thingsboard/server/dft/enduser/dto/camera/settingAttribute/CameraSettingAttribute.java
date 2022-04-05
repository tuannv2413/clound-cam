package org.thingsboard.server.dft.enduser.dto.camera.settingAttribute;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDetailDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraEditDto;
import org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.setting.ProfileRtsp;

import java.util.*;

@Data
@NoArgsConstructor
public class CameraSettingAttribute {
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private UUID tbDeviceId;

  private UUID cameraId;
  private String ipv4;
  private String channel;
  private List<ProfileRtsp> profiles;
  private String onvifUsername;
  private String onvifPassword;
  private int onvifPort;
  private String rtmpUrl;
  private boolean isOnvif;

  public CameraSettingAttribute(CameraEditDto cameraEditDto) {
    this.cameraId = cameraEditDto.getId();
    this.ipv4 = cameraEditDto.getIpv4();
    this.channel = cameraEditDto.getChannel();
    List<ProfileRtsp> profiles = new ArrayList<>();
    profiles.add(new ProfileRtsp(this.channel, cameraEditDto.getMainRtspUrl()));
    profiles.add(new ProfileRtsp("subStream", cameraEditDto.getSubRtspUrl()));
    this.profiles = profiles;
    this.onvifUsername = cameraEditDto.getOnvifUsername();
    this.onvifPassword = cameraEditDto.getOnvifPassword();
    this.onvifPort = cameraEditDto.getOnvifPort();
    this.rtmpUrl = cameraEditDto.getRtmpUrl();
    this.isOnvif = this.onvifUsername != null && !this.onvifUsername.equals("");
  }

  public boolean getIsOnvif() {
    return isOnvif;
  }
}
