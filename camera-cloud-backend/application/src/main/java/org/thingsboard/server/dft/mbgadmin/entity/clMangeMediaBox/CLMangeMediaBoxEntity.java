package org.thingsboard.server.dft.mbgadmin.entity.clMangeMediaBox;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.thingsboard.server.dft.enduser.entity.BaseInfoEnity;
import org.thingsboard.server.dft.mbgadmin.entity.clBox.CLBoxEntity;
import org.thingsboard.server.dft.mbgadmin.entity.clTenant.CLTenantEntity;
import org.thingsboard.server.dft.mbgadmin.entity.tenantQL.TenantEntityQL;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cl_mange_media_box")
public class CLMangeMediaBoxEntity extends BaseInfoEnity implements Serializable {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

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

//    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @JoinColumn(name = "cl_box_id")
//    private CLBoxEntity clBoxEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private TenantEntityQL tenantEntityQL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cl_tenant_id")
    private CLTenantEntity clTenantEntity;
}
