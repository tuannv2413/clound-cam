package org.thingsboard.server.dft.enduser.entity.camera;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@Entity
@Table(name = "cl_customer_camera_permission")
public class CustomerCameraPermissionEntity{

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "cl_user_id", columnDefinition = "uuid")
    private UUID clUserId;

    @Column(name = "cl_camera_id", columnDefinition = "uuid")
    private UUID clCameraId;

    @Column(name = "live")
    private boolean live;

    @Column(name = "history")
    private boolean history;

    @Column(name = "ptz")
    private boolean ptz;
}
