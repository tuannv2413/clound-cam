package org.thingsboard.server.dft.enduser.dto.cameraGroup;

import lombok.Data;

import java.util.UUID;

@Data
public class CameraGroupGetAllResponse {
    private UUID cameraGroupId;
    private String groupName;
    private Boolean isDefault;
}
