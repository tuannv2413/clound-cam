package org.thingsboard.server.dft.enduser.dto.camera;

import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.thingsboard.server.dft.enduser.dto.cameraGroup.CameraIdDto;

import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddCameraGroupDto {
    @NotNull
    private UUID cameraGroupId;

    @NotNull
    private List<CameraIdDto> cameraIdList;
}
