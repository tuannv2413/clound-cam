package org.thingsboard.server.dft.mbgadmin.entity.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@Table(name = "cl_tbuser_and_role")
@Entity
@IdClass(UserRoleId.class)
public class UserRoleEntity implements Serializable {
    @Id
    @Column(name = "tb_user_id")
    private UUID userId;

    @Id
    @Column(name = "cl_role_id")
    private UUID roleId;
}
