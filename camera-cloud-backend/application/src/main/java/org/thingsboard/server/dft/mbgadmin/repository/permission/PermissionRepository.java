package org.thingsboard.server.dft.mbgadmin.repository.permission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.mbgadmin.entity.permission.PermissionEntity;

import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID>  {
}
