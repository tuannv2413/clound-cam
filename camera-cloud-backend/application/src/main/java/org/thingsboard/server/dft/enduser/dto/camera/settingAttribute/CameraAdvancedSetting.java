package org.thingsboard.server.dft.enduser.dto.camera.settingAttribute;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDetailDto;
import org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.setting.ResolutionSupport;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CameraAdvancedSetting {
  private UUID cameraId;
  private String channel;
//  private String rtspUrl;
  private ResolutionSupport resolution;
  private int fps;
  private long bitrate;
  private boolean fisheye;

  public CameraAdvancedSetting(CameraDetailDto cameraDetailDto) {
    this.cameraId = cameraDetailDto.getId();
    this.channel = cameraDetailDto.getChannel();
    this.fps = cameraDetailDto.getFps();
    this.resolution = cameraDetailDto.getResolutionSetting();
    this.bitrate = cameraDetailDto.getBitrate();
    this.fisheye = cameraDetailDto.isFisheye();
  }
}
