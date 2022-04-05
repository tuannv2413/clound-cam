package org.thingsboard.server.dft.mbgadmin.entity.tenantProfile;

import com.eclipsesource.json.Json;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.thingsboard.server.dft.mbgadmin.entity.tenantQL.TenantEntityQL;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tenant_profile")
public class TenantProfileEntityQL implements Serializable {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "created_time")
    private long createdTime;

    @Column(name = "name")
    private String name;

//    @Column(name = "profile_data")
//    private String profileData;

    @Column(name = "description")
    private String description;

    @Column(name = "search_text")
    private String searchText;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "isolated_tb_core")
    private Boolean isolatedTbCore;

    @Column(name = "isolated_tb_rule_engine")
    private Boolean isolatedTbRuleEngine;

    @OneToOne(mappedBy = "tenantProfileEntityQL", cascade = CascadeType.ALL)
    private TenantEntityQL tenantEntityQL;
}
