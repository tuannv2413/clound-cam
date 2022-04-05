package org.thingsboard.server.dft.mbgadmin.entity.mediaBox;

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
@Table(name = "cl_mange_media_box")
public class MediaBoxEntity extends BaseInfoEnity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "origin_serial_number")
    private String originSerialNumber;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "part_number")
    private String partNumber;

    @Column(name = "consignment")
    private String consignment;

    @Column(name = "type")
    private String type;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column(name = "status")
    private String status;

    @Column(name = "cl_tenant_id")
    private UUID clTenantId;
}
