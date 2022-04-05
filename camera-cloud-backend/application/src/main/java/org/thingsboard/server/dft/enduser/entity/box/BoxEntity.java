package org.thingsboard.server.dft.enduser.entity.box;

import lombok.*;
import org.hibernate.Hibernate;
import org.thingsboard.server.dft.enduser.entity.BaseInfoEnity;
import org.thingsboard.server.dft.enduser.entity.camera.CameraEntity;
import org.thingsboard.server.dft.mbgadmin.entity.mediaBox.MediaBoxEntity;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "cl_box")
public class BoxEntity extends BaseInfoEnity {
  @Id
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "uuid")
  private UUID tenantId;

  @Column(name = "box_name")
  private String boxName;

  @Column(name = "serial_number")
  private String serialNumber;

  @Column(name = "tb_device_id")
  private UUID tbDeviceId;

  @Column(name = "is_delete")
  private boolean isDelete;

  @OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
  @JoinColumn(name = "media_box_id", referencedColumnName = "id")
  @ToString.Exclude
  private MediaBoxEntity mediaBoxEntity;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "boxEntity", fetch = FetchType.LAZY)
  @ToString.Exclude
  private Set<CameraEntity> cameraEntitySet;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
    BoxEntity boxEntity = (BoxEntity) o;
    return id != null && Objects.equals(id, boxEntity.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
