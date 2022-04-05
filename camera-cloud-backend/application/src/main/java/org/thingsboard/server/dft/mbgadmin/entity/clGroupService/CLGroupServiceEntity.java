package org.thingsboard.server.dft.mbgadmin.entity.clGroupService;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.thingsboard.server.dft.enduser.entity.BaseInfoEnity;
import org.thingsboard.server.dft.mbgadmin.entity.clServiceOption.CLServiceOptionEntity;
import org.thingsboard.server.dft.mbgadmin.entity.clTenant.CLTenantEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cl_group_service")
public class CLGroupServiceEntity extends BaseInfoEnity implements Serializable {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "max_day_storage")
    private int maxDayStorage;

    @Column(name = "note")
    private String note;

    @Column(name = "active")
    private Boolean active;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "clGroupServiceEntity", cascade = CascadeType.ALL)
    private List<CLServiceOptionEntity> clServiceOptionEntities;

}
