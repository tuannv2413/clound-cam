package org.thingsboard.server.dft.mbgadmin.entity.tenantQL;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.thingsboard.server.dft.mbgadmin.entity.clBox.CLBoxEntity;
import org.thingsboard.server.dft.mbgadmin.entity.clUser.CLUserEntity;
import org.thingsboard.server.dft.mbgadmin.entity.tenantProfile.TenantProfileEntityQL;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tenant")
public class TenantEntityQL implements Serializable {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "created_time")
    private long createdTime;

    @Column(name = "additional_info")
    private String additionalInfo;

    @OneToOne
    @JoinColumn(name = "tenant_profile_id", referencedColumnName = "id")
    private TenantProfileEntityQL tenantProfileEntityQL;

    @Column(name = "address")
    private String address;

    @Column(name = "address2")
    private String address2;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "region")
    private String region;

    @Column(name = "search_text")
    private String searchText;

    @Column(name = "state")
    private String state;

    @Column(name = "title")
    private String title;

    @Column(name = "zip")
    private String zip;

    @OneToMany(mappedBy = "tenantEntityQL")
    private List<CLBoxEntity> clBoxEntities;

    @OneToMany(mappedBy = "tenantEntityQL")
    private List<CLUserEntity> clUserEntities;

}
