package org.thingsboard.server.dft.enduser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.entity.BaseInfoEnity;

import java.util.UUID;

@Data
@NoArgsConstructor
public abstract class BaseInfoDto {

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private long createdTime;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private UUID createdBy;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private long updatedTime;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private UUID updatedBy;

  public BaseInfoDto(BaseInfoEnity baseInfoEnity) {
    this.createdTime = baseInfoEnity.getCreatedTime();
    this.createdBy = baseInfoEnity.getCreatedBy();
    this.updatedTime = baseInfoEnity.getUpdatedTime();
    this.updatedBy = baseInfoEnity.getUpdatedBy();
  }
}
