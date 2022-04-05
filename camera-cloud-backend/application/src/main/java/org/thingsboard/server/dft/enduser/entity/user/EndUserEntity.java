package org.thingsboard.server.dft.enduser.entity.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.dft.enduser.entity.BaseInfoEnity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cl_user")
public class EndUserEntity extends BaseInfoEnity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "active")
    private boolean active;

    @Column(name = "office")
    private String office;

    @Column(name = "search_text")
    private String searchText;

    @Column(name = "type")
    private Authority type;

    @Column(name = "delete")
    private boolean delete;

    @Column(name = "tb_user_id")
    private UUID userId;

    @Column(name = "email")
    private String email;

    @Column(name = "avatar")
    private String avatar;

//    @OneToMany(mappedBy = "endUserEntity")
//    Set<CustomerCameraPermissionEntity> permissionEntitySet;
}
