package org.thingsboard.server.dft.enduser.dto.camera.settingAttribute;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.entity.camera.recordSetting.RecordSetting;

import java.util.UUID;

@Data
@NoArgsConstructor
public class SettingRecordGroupAttribute {
  private UUID cameraId;
  private RecordSetting recordSetting;
}
