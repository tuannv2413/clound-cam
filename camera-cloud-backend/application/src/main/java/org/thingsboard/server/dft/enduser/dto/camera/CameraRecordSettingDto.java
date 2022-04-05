package org.thingsboard.server.dft.enduser.dto.camera;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.thingsboard.server.dft.enduser.dto.BaseInfoDto;
import org.thingsboard.server.dft.enduser.entity.camera.CameraRecordSettingEntity;
import org.thingsboard.server.dft.enduser.entity.camera.recordSetting.RecordSetting;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@EqualsAndHashCode(callSuper=false)
@Data
@NoArgsConstructor
public class CameraRecordSettingDto extends BaseInfoDto {

  private UUID id;

  @NotBlank
  @NotNull
  private UUID cameraId;

  @NotNull
  @NotBlank
  private String name;

  private String type;
  private RecordSetting setting;
  private boolean active;
  private UUID rawSettingId;

  public CameraRecordSettingDto(CameraRecordSettingEntity cameraRecordSettingEntity) throws JsonProcessingException {
    super(cameraRecordSettingEntity);
    this.id = cameraRecordSettingEntity.getId();
    this.cameraId = cameraRecordSettingEntity.getCameraId();
    this.name = cameraRecordSettingEntity.getName();
    this.type = cameraRecordSettingEntity.getType();
    ObjectMapper mapper = new ObjectMapper();
    RecordSetting setting = mapper.treeToValue(cameraRecordSettingEntity.getSetting(), RecordSetting.class);
    this.setting = setting;
    this.active = cameraRecordSettingEntity.isActive();
    this.rawSettingId = cameraRecordSettingEntity.getRawSettingId();
  }
}
