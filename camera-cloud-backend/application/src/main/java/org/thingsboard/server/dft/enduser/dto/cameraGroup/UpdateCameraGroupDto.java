package org.thingsboard.server.dft.enduser.dto.cameraGroup;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateCameraGroupDto {
    @NotBlank
    private String groupName;
    private Boolean isDefault = false;
}
