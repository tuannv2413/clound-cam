package org.thingsboard.server.dft.enduser.dto.cameraGroup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.thingsboard.server.dft.enduser.entity.cameraGroup.CameraGroupEntity;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CameraGroupDto {

    private UUID id;
    private UUID tenantID;

    @NotNull
    @Length(max = 250, message = "Tên nhóm không nhập quá 255")
    private String groupName;
    private String indexSetting;
    private Boolean isDefault = false;

    @NotNull
    private List<CameraIdDto> cameraIdList;

    public CameraGroupDto(CameraGroupEntity cameraGroupEntity) {
        this.id = cameraGroupEntity.getId();
        this.tenantID = cameraGroupEntity.getTenantId();
        this.groupName = cameraGroupEntity.getCameraGroupName();
        this.indexSetting = cameraGroupEntity.getIndexSetting();
        this.isDefault = cameraGroupEntity.isDefault();
    }
}
