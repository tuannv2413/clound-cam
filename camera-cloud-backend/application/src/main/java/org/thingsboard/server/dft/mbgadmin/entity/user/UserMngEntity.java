package org.thingsboard.server.dft.mbgadmin.entity.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.dft.mbgadmin.entity.BaseEnity;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "cl_user")
public class UserMngEntity extends BaseEnity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

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
    private boolean active;

    @Column(name = "delete")
    private boolean delete;

    @Column(name = "type")
    private Authority type;

    @Column(name = "tb_user_id")
    private UUID tbUserId;

//    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
//    @JoinTable(name = "users_roles", joinColumns = {
//            @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, updatable = false) }, inverseJoinColumns = {
//            @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false, updatable = false) })
//    Set<RoleEntity> roles;
}
