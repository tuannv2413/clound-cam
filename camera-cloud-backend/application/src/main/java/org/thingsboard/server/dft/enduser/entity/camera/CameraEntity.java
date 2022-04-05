package org.thingsboard.server.dft.enduser.entity.camera;


import lombok.*;
import org.hibernate.Hibernate;
import org.thingsboard.server.dft.enduser.entity.BaseInfoEnity;
import org.thingsboard.server.dft.enduser.entity.box.BoxEntity;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "cl_camera")
public class CameraEntity extends BaseInfoEnity {
  @Id
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "uuid")
  private UUID tenantId;

  @Column(name = "camera_group_id", columnDefinition = "uuid")
  private UUID cameraGroupId;

  @Column(name = "camera_name")
  private String cameraName;

  @Column(name = "ipv4")
  private String ipv4;

  @Column(name = "camera_fisheye")
  private boolean cameraFisheye;

  @Column(name = "rtsp_username")
  private String rtspUsername;

  @Column(name = "rtsp_password")
  private String rtspPassword;

  @Column(name = "rtsp_port")
  private int rtspPort;

  @Column(name = "main_rtsp_url")
  private String mainRtspUrl;

  @Column(name = "sub_rtsp_url")
  private String subRtspUrl;

  @Column(name = "onvif_username")
  private String onvifUsername;

  @Column(name = "onvif_password")
  private String onvifPassword;

  @Column(name = "onvif_port")
  private int onvifPort;

  @Column(name = "onvif_url")
  private String onvifUrl;

  @Column(name = "rtmp_stream_id")
  private String rtmpStreamId;

  @Column(name = "rtmp_url")
  private String rtmpUrl;

  @Column(name = "tb_device_id")
  private UUID tbDeviceId;

  @Column(name = "is_delete")
  private boolean isDelete;

  @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
  @JoinColumn(name = "box_id", referencedColumnName = "id")
  @ToString.Exclude
  private BoxEntity boxEntity;

//  @OneToMany(mappedBy = "cameraEntity")
//  Set<CustomerCameraPermissionEntity> permissionEntitySet;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    CameraEntity that = (CameraEntity) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
