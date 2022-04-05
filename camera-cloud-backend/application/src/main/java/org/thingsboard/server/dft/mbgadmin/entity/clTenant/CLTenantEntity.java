package org.thingsboard.server.dft.mbgadmin.entity.clTenant;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.thingsboard.server.dft.enduser.entity.BaseInfoEnity;
import org.thingsboard.server.dft.mbgadmin.entity.clMangeMediaBox.CLMangeMediaBoxEntity;
import org.thingsboard.server.dft.mbgadmin.entity.clServiceOption.CLServiceOptionEntity;
import org.thingsboard.server.dft.mbgadmin.entity.clUser.CLUserEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cl_tenant")
public class CLTenantEntity extends BaseInfoEnity implements Serializable {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "code")
    private String code;

    @Column(name = "type")
    private String type;

    @Column(name = "day_start_service")
    private Long dayStartService;

    @Column(name = "address")
    private String address;

    @Column(name = "note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cl_user_id")
    private CLUserEntity clUserEntity;

    @OneToMany(mappedBy = "clTenantEntity")
    private List<CLMangeMediaBoxEntity> clMangeMediaBoxEntities;

    @ManyToMany
    @JoinTable(
            name = "cl_service_option_and_cltenant",
            joinColumns = @JoinColumn(name = "cl_tenant_id"),
            inverseJoinColumns = @JoinColumn(name = "service_option_id")
    )
    private Set<CLServiceOptionEntity> clServiceOptionEntities;

}
