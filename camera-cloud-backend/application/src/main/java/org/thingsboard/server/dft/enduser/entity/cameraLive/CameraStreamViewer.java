package org.thingsboard.server.dft.enduser.entity.cameraLive;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "cl_camera_stream_viewer")
public class CameraStreamViewer {
  @Id
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "camera_id", columnDefinition = "uuid")
  private UUID cameraId;

  @Column(name = "tb_user_id")
  private UUID tbUserId;

  @Column(name = "status")
  private boolean status;

  @Column(name = "last_ping")
  private long lastPing;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    CameraStreamViewer that = (CameraStreamViewer) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
