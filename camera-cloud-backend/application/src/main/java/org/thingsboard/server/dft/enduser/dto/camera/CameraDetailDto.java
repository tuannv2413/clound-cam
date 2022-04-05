package org.thingsboard.server.dft.enduser.dto.camera;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.setting.ResolutionSupport;
import org.thingsboard.server.dft.enduser.entity.camera.CameraEntity;
import org.thingsboard.server.dft.mbgadmin.dto.setting.AntMediaServerDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CameraDetailDto {
  private UUID id;
  private UUID tenantId;
  private UUID tbDeviceId;
  private String cameraName;
  private UUID boxId;
  private String boxName;
  private boolean active;
  private String ipv4;
  private String rtspUsername;
  private String rtspPassword;
  //  private int rtspPort;
  private String onvifUsername;
  private String onvifPassword;
  private int onvifPort;
  private String onvifUrl;
  private String channel;
  private String resolution;
  private ResolutionSupport resolutionSetting;
  private int fps;
  private long bitrate;
  private boolean fisheye;
  private boolean fisheyeSupport;
  private List<String> channelSupport;
  private Map<String, CameraAdvanceSettingOption> optionSetting;

  private String mainRtspUrl;
  private String subRtspUrl;
  private String rtmpUrl;
  private String rtmpStreamId;
  private String streamViewUrl;
  private String streamHlsUrl;
  private String webSocketUrl;

  private String groupName;
  private UUID groupId;

  public CameraDetailDto(CameraEntity cameraEntity) {
    this.id = cameraEntity.getId();
    this.tenantId = cameraEntity.getTenantId();
    this.tbDeviceId = cameraEntity.getTbDeviceId();
    this.cameraName = cameraEntity.getCameraName();
    this.boxId = cameraEntity.getBoxEntity().getId();
    this.boxName = cameraEntity.getBoxEntity().getBoxName();
    this.ipv4 = cameraEntity.getIpv4();
    this.rtspUsername = cameraEntity.getRtspUsername();
    this.rtspPassword = cameraEntity.getRtspPassword();
//    this.rtspPort = cameraEntity.getRtspPort();
    this.onvifUsername = cameraEntity.getOnvifUsername();
    this.onvifPassword = cameraEntity.getOnvifPassword();
    this.onvifPort = cameraEntity.getOnvifPort();
    this.onvifUrl = cameraEntity.getOnvifUrl();
    this.mainRtspUrl = cameraEntity.getMainRtspUrl();
    this.subRtspUrl = cameraEntity.getSubRtspUrl();
    this.rtmpStreamId = cameraEntity.getRtmpStreamId();
    this.groupId = cameraEntity.getCameraGroupId();
  }

  public CameraDetailDto(CameraEntity cameraEntity, AntMediaServerDto antMediaServerDto) {
    this.id = cameraEntity.getId();
    this.tenantId = cameraEntity.getTenantId();
    this.tbDeviceId = cameraEntity.getTbDeviceId();
    this.cameraName = cameraEntity.getCameraName();
    this.boxId = cameraEntity.getBoxEntity().getId();
    this.boxName = cameraEntity.getBoxEntity().getBoxName();
    this.ipv4 = cameraEntity.getIpv4();
    this.rtspUsername = cameraEntity.getRtspUsername();
    this.rtspPassword = cameraEntity.getRtspPassword();
//    this.rtspPort = cameraEntity.getRtspPort();
    this.onvifUsername = cameraEntity.getOnvifUsername();
    this.onvifPassword = cameraEntity.getOnvifPassword();
    this.onvifPort = cameraEntity.getOnvifPort();
    this.mainRtspUrl = cameraEntity.getMainRtspUrl();
    this.subRtspUrl = cameraEntity.getSubRtspUrl();
    this.rtmpStreamId = cameraEntity.getRtmpStreamId();
    this.streamViewUrl = builStringStreamUrl(this.rtmpStreamId, antMediaServerDto.getHttpUrl());
    this.streamHlsUrl = buildHslUrl(this.rtmpStreamId, antMediaServerDto.getHttpUrl());
    this.webSocketUrl = antMediaServerDto.getWebSocketUrl();
  }

  private String builStringStreamUrl(String rtmpStreamId, String antUrl) {
    return antUrl + "/play.html?name=" + rtmpStreamId;
  }

  private String buildHslUrl(String rtmpStreamId, String antUrl) {
    return antUrl + "/streams/" + rtmpStreamId + ".m3u8";
  }
}