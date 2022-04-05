package org.thingsboard.server.dft.mbgadmin.entity.clUser;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.thingsboard.server.dft.enduser.entity.BaseInfoEnity;
import org.thingsboard.server.dft.mbgadmin.entity.clTenant.CLTenantEntity;
import org.thingsboard.server.dft.mbgadmin.entity.tenantQL.TenantEntityQL;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cl_user")
public class CLUserEntity extends BaseInfoEnity implements Serializable {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "office")
    private String office;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "search_text")
    private String searchText;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "type")
    private String type;

    @Column(name = "delete")
    private Boolean delete;

    @Column(name = "tb_user_id")
    private UUID tbUserId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "tenant_id")
    private TenantEntityQL tenantEntityQL;

    @OneToMany(mappedBy = "clUserEntity", cascade = CascadeType.ALL)
    private List<CLTenantEntity> clTenantEntities;
}
