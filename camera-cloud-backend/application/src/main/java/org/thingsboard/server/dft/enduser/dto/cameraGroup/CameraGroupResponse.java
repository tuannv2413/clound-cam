package org.thingsboard.server.dft.enduser.dto.cameraGroup;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class CameraGroupResponse {

    private UUID id;
    private String groupName;
    private Long groupCameraNumber;
    private String cameraName;
    private String createName;
    private UUID createId;
    private Long createTime;
    private Boolean isDefault;
    private UUID updateId;
    private String updateName;
    private Long updateTime;
}
