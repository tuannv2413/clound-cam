package org.thingsboard.server.dft.mbgadmin.entity.permission;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "cl_permission")
public class PermissionEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "permission")
    private String permission;
}
