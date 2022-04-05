package org.thingsboard.server.dft.enduser.dto.cameraGroup;

import lombok.*;
import org.thingsboard.server.dft.enduser.entity.cameraGroup.CameraGroupEntity;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CameraGroupList {

    private UUID id;
    private String groupName;
    private long groupCameraNumber;
    private String cameraName;
    private String createName;
    private UUID createId;
    private Boolean isDefault;
    private long createTime;

    public CameraGroupList(CameraGroupEntity cameraGroupEntity) {
        this.groupName = cameraGroupEntity.getCameraGroupName();
        this.createId = cameraGroupEntity.getCreatedBy();
        this.id = cameraGroupEntity.getId();
        this.isDefault = cameraGroupEntity.isDefault();
        this.createTime = cameraGroupEntity.getCreatedTime();
    }
}
