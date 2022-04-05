package org.thingsboard.server.dft.enduser.dto.box;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.BaseInfoDto;
import org.thingsboard.server.dft.enduser.entity.box.BoxEntity;

import java.util.UUID;

@Data
@NoArgsConstructor
public class BoxDetailDto extends BaseInfoDto {
  private UUID id;
  private String boxName;
  private String serialNumber;
  //    private UUID locationId;
  private double cpu;
  private double ram;
  private double temperature;
  private boolean active;
  private long lastActivityTime; // Thời gian cloud ghi nhận box không còn kết nối
  private long lastConnectTime; // Thời gian kết nối cuối từ box đến cloud
  private String ipv4;
  private String firmware;
  private String model;
  private int totalCamera;
  private UUID tbDeviceId;

  public BoxDetailDto(BoxEntity boxEntity) {
    super(boxEntity);
    this.id = boxEntity.getId();
    this.boxName = boxEntity.getBoxName();
    this.serialNumber = boxEntity.getSerialNumber();
    this.tbDeviceId = boxEntity.getTbDeviceId();
  }
}
