package org.thingsboard.server.dft.enduser.entity.camera;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.thingsboard.server.dao.util.mapping.JsonStringType;
import org.thingsboard.server.dft.enduser.entity.BaseInfoEnity;

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
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Table(name = "cl_camera_record_settting")
public class CameraRecordSettingEntity extends BaseInfoEnity {

  @Id
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "uuid")
  private UUID tenantId;

  @Column(name = "camera_id")
  private UUID cameraId;

  @Column(name = "name")
  private String name;

  @Column(name = "type")
  private String type;

  @Column(name = "setting")
  @Type(type = "json")
  private JsonNode setting;

  @Column(name = "active")
  private boolean active;

  @Column(name = "raw_setting_id", columnDefinition = "uuid")
  private UUID rawSettingId;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    CameraRecordSettingEntity that = (CameraRecordSettingEntity) o;
    return id != null && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
