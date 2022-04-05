package org.thingsboard.server.dft.enduser.dto.camera;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.BaseInfoDto;
import org.thingsboard.server.dft.enduser.entity.camera.CameraEntity;

import java.util.UUID;

@EqualsAndHashCode(callSuper=false)
@Data
@NoArgsConstructor
public class CameraDto extends BaseInfoDto {
  private UUID id;
  private UUID tbDeviceId;
  private String cameraName;
  private UUID boxId;
  private String boxName;
  private String ipv4;
  private String resolution;
  private UUID groupId;
  private String groupName;
//  private UUID place;


  public CameraDto(CameraEntity cameraEntity) {
    super(cameraEntity);
    this.id = cameraEntity.getId();
    this.tbDeviceId = cameraEntity.getTbDeviceId();
    this.cameraName = cameraEntity.getCameraName();
    this.boxId = cameraEntity.getBoxEntity().getId();
    this.boxName = cameraEntity.getBoxEntity().getBoxName();
    this.ipv4 = cameraEntity.getIpv4();
    if (cameraEntity.getCameraGroupId() != null) {
      this.groupId = cameraEntity.getCameraGroupId();
    }
  }
}
