package org.thingsboard.server.dft.mbgadmin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.mbgadmin.entity.BaseEnity;

import java.util.UUID;

@Data
@NoArgsConstructor
public abstract class BaseDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long createdTime;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID createdBy;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long updatedTime;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID updatedBy;

    public BaseDto(BaseEnity baseEnity) {
        this.createdTime = baseEnity.getCreatedTime();
        this.createdBy = baseEnity.getCreatedBy();
        this.updatedTime = baseEnity.getUpdatedTime();
        this.updatedBy = baseEnity.getUpdatedBy();
    }
}
