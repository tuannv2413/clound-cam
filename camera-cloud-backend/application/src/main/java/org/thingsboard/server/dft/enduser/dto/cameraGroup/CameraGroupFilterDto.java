package org.thingsboard.server.dft.enduser.dto.cameraGroup;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDto;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CameraGroupFilterDto {
    private UUID id;
    private UUID tenantId;
    private String cameraGroupName;
    private boolean isDefault;
    private List<CameraDto> listCameras;
}
