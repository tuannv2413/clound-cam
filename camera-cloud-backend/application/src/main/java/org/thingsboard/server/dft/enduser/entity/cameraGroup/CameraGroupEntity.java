package org.thingsboard.server.dft.enduser.entity.cameraGroup;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.thingsboard.server.dft.enduser.entity.BaseInfoEnity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "cl_camera_group")
public class CameraGroupEntity extends BaseInfoEnity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "camera_group_name")
    private String cameraGroupName;

    @Type(type = "jsonb")
    @Column(name = "index_setting", columnDefinition = "json")
    private String indexSetting;

    @Column(name = "is_default")
    private boolean isDefault;

//     = "{\"id\": 1,\"name\": \"123\"}"
}
