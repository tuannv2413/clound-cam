package org.thingsboard.server.dft.enduser.entity.camera.recordSetting;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.util.constant.CameraInfoKeyConstant;

import java.util.List;

@ApiModel
@Data
@NoArgsConstructor
public class RecordSetting {
  private List<TimeRecordSetting> T2;
  private List<TimeRecordSetting> T3;
  private List<TimeRecordSetting> T4;
  private List<TimeRecordSetting> T5;
  private List<TimeRecordSetting> T6;
  private List<TimeRecordSetting> T7;
  private List<TimeRecordSetting> CN;

  public RecordSetting(String type) {
    if (type.equals(CameraInfoKeyConstant.FULL_TIME)) {
      this.setT2(getFullTimeDefault());
      this.setT3(getFullTimeDefault());
      this.setT4(getFullTimeDefault());
      this.setT5(getFullTimeDefault());
      this.setT6(getFullTimeDefault());
      this.setT7(getFullTimeDefault());
      this.setCN(getFullTimeDefault());
    } else if (type.equals(CameraInfoKeyConstant.INACTIVE_TIME)) {
      this.setT2(getDisableTimeDefault());
      this.setT3(getDisableTimeDefault());
      this.setT4(getDisableTimeDefault());
      this.setT5(getDisableTimeDefault());
      this.setT6(getDisableTimeDefault());
      this.setT7(getDisableTimeDefault());
      this.setCN(getDisableTimeDefault());
    }
  }

  private List<TimeRecordSetting> getFullTimeDefault() {
    TimeRecordSetting timeRecordSetting = new TimeRecordSetting();
    timeRecordSetting.setStartTime(0);
    timeRecordSetting.setEndTime(86400000 - 1);
    return List.of(timeRecordSetting);
  }

  private List<TimeRecordSetting> getDisableTimeDefault() {
    TimeRecordSetting timeRecordSetting = new TimeRecordSetting();
    timeRecordSetting.setEndTime(0);
    timeRecordSetting.setStartTime(0);
    return List.of(timeRecordSetting);
  }


  private long getDefaultStartTime() {
    return 0;
  }

  private long getDefaultEndTime() {
    return 86400000 - 1;
  }
}
