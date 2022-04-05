package org.thingsboard.server.dft.enduser.dto.camera;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.entity.camera.CameraEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CameraEditDto {

  private UUID id;

  private UUID tenantId;
  private UUID tbDeviceId;
  private UUID cameraGroupId;

  @NotNull
  @NotBlank
  private String cameraName;

  private String ipv4;

  private int rtspPort;
  private String rtspUsername;
  private String rtspPassword;

  private int onvifPort;
  private String onvifUsername;
  private String onvifPassword;

  private boolean fishEye;

  @NotNull
  private UUID boxId;

  private String mainRtspUrl;
  private String subRtspUrl;
  private String onvifUrl;

  private String rtmpStreamId;
  private String rtmpUrl;
  private String channel;


  public CameraEditDto(CameraEntity cameraEntity) {
    this.id = cameraEntity.getId();
    this.tenantId = cameraEntity.getTenantId();
    this.cameraGroupId = cameraEntity.getCameraGroupId();
    this.cameraName = cameraEntity.getCameraName();
    this.ipv4 = cameraEntity.getIpv4();
    this.rtspPort = cameraEntity.getRtspPort();
    this.rtspUsername = cameraEntity.getRtspUsername();
    this.rtspPassword = cameraEntity.getRtspPassword();
    this.onvifPort = cameraEntity.getOnvifPort();
    this.onvifUsername = cameraEntity.getOnvifUsername();
    this.onvifPassword = cameraEntity.getOnvifPassword();
    this.onvifUrl = cameraEntity.getOnvifUrl();
    this.fishEye = cameraEntity.isCameraFisheye();
    this.boxId = cameraEntity.getBoxEntity().getId();
    this.mainRtspUrl = cameraEntity.getMainRtspUrl();
    this.subRtspUrl = cameraEntity.getSubRtspUrl();
    this.tbDeviceId = cameraEntity.getTbDeviceId();
    this.rtmpStreamId = cameraEntity.getRtmpStreamId();
    this.rtmpUrl = cameraEntity.getRtmpUrl();
  }

  public CameraEditDto(CameraDetailDto cameraDetailDto) {
    this.id = cameraDetailDto.getId();
    this.cameraName = cameraDetailDto.getCameraName();
    this.ipv4 = cameraDetailDto.getIpv4();
//    this.rtspPort = cameraDetailDto.getRtspPort();
    this.rtspUsername = cameraDetailDto.getRtspUsername();
    this.rtspPassword = cameraDetailDto.getRtspPassword();
    this.onvifPort = cameraDetailDto.getOnvifPort();
    this.onvifUsername = cameraDetailDto.getOnvifUsername();
    this.onvifPassword = cameraDetailDto.getOnvifPassword();
    this.boxId = cameraDetailDto.getBoxId();
    this.tbDeviceId = cameraDetailDto.getTbDeviceId();
    this.channel = cameraDetailDto.getChannel();
    this.mainRtspUrl = cameraDetailDto.getMainRtspUrl();
    this.subRtspUrl = cameraDetailDto.getSubRtspUrl();
    this.channel = cameraDetailDto.getChannel();
  }
}
