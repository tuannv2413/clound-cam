package org.thingsboard.server.dft.mbgadmin.entity.clServiceOption;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.thingsboard.server.dft.mbgadmin.entity.clGroupService.CLGroupServiceEntity;
import org.thingsboard.server.dft.mbgadmin.entity.clTenant.CLTenantEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cl_service_option")
public class CLServiceOptionEntity implements Serializable {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "resolution")
    private String resolution;

    @Column(name = "price")
    private Long price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_service_id")
    private CLGroupServiceEntity clGroupServiceEntity;

    @ManyToMany
    @JoinTable(
            name = "cl_service_option_and_cltenant",
            joinColumns = @JoinColumn(name = "service_option_id"),
            inverseJoinColumns = @JoinColumn(name = "cl_tenant_id")
    )
    private Set<CLTenantEntity> clTenantEntities;
}
