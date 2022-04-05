package org.thingsboard.server.dft.mbgadmin.repository.tenantProfile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.mbgadmin.entity.tenantProfile.TenantProfileEntityQL;

import java.util.UUID;

@Repository
public interface TenantProfileRepositoryQL extends JpaRepository<TenantProfileEntityQL, UUID> {
}
