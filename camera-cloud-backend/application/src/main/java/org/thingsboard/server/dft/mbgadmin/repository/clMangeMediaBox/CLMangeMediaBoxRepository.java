package org.thingsboard.server.dft.mbgadmin.repository.clMangeMediaBox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.mbgadmin.entity.clMangeMediaBox.CLMangeMediaBoxEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CLMangeMediaBoxRepository extends JpaRepository<CLMangeMediaBoxEntity, UUID> {
    List<CLMangeMediaBoxEntity> findAllByClTenantEntityId(UUID clTenantId);
}
