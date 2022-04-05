package org.thingsboard.server.dft.enduser.dto.camera;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCameraPermissionDto {

    private UUID userID;
    private UUID clCameraId;
    private Boolean live;
    private Boolean history;
    private Boolean ptz;
    private String cameraName;
}
