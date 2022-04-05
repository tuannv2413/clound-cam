package org.thingsboard.server.dft.mbgadmin.entity.role;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.thingsboard.server.dft.enduser.entity.BaseInfoEnity;
import org.thingsboard.server.dft.mbgadmin.entity.permission.PermissionEntity;
import org.thingsboard.server.dft.mbgadmin.entity.tbUser.TbUserEntity;
import org.thingsboard.server.dft.mbgadmin.entity.user.UserMngEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cl_role")
public class RoleEntity extends BaseInfoEnity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "role_name")
    private String roleName;

    @Column(name = "role_type")
    private String roleType;

    @Column(name = "note")
    private String note;

    @ManyToMany(cascade = CascadeType.PERSIST,fetch = FetchType.EAGER)
    @JoinTable(
            name = "cl_role_and_permission",
            joinColumns = @JoinColumn(name = "cl_role_id"),
            inverseJoinColumns = @JoinColumn(name = "cl_permission_id")
    )
    private Set<PermissionEntity> permissionEntities = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @JsonIgnoreProperties
    @JoinTable(
            name = "cl_tbuser_and_role",
            joinColumns = @JoinColumn(name = "cl_role_id"),
            inverseJoinColumns = @JoinColumn(name = "tb_user_id")
    )
    private Set<UserMngEntity> userMngEntities = new HashSet<>();

    @ManyToMany(cascade = CascadeType.PERSIST,fetch = FetchType.EAGER)
    @JoinTable(
            name = "cl_tbuser_and_role",
            joinColumns = @JoinColumn(name = "cl_role_id"),
            inverseJoinColumns = @JoinColumn(name = "tb_user_id")
    )
    private Set<TbUserEntity> tbUserEntity = new HashSet<>();
}
