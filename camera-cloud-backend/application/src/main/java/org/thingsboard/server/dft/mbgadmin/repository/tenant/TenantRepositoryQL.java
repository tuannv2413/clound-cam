package org.thingsboard.server.dft.mbgadmin.repository.tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.mbgadmin.entity.tenantQL.TenantEntityQL;

import java.util.UUID;

@Repository
public interface TenantRepositoryQL extends JpaRepository<TenantEntityQL, UUID> {
}
