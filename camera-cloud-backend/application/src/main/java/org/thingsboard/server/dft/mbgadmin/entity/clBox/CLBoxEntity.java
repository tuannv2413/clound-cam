package org.thingsboard.server.dft.mbgadmin.entity.clBox;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.thingsboard.server.dft.enduser.entity.BaseInfoEnity;
import org.thingsboard.server.dft.mbgadmin.entity.clMangeMediaBox.CLMangeMediaBoxEntity;
import org.thingsboard.server.dft.mbgadmin.entity.tenantQL.TenantEntityQL;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cl_box")
public class CLBoxEntity extends BaseInfoEnity implements Serializable {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "box_name")
    private String boxName;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "tb_device_id")
    private String tbDeviceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private TenantEntityQL tenantEntityQL;

//    @OneToMany(mappedBy = "clBoxEntity", cascade = CascadeType.ALL)
//    private List<CLMangeMediaBoxEntity> clMangeMediaBoxEntities;

}
