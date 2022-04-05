package org.thingsboard.server.dft.mbgadmin.repository.clServiceOption;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.mbgadmin.entity.clServiceOption.CLServiceOptionEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CLServiceOptionRepository extends JpaRepository<CLServiceOptionEntity, UUID> {
    CLServiceOptionEntity findByClGroupServiceEntityId(UUID id);
    CLServiceOptionEntity findByClGroupServiceEntityName(String name);

    @Query(value = "select Cast(a.cl_tenant_id as varchar) cl_tenant_id from cl_service_option_and_cltenant a where a.service_option_id = :serviceOptionId",nativeQuery = true)
    List<String> checkGroupService(@Param("serviceOptionId") UUID serviceOptionId);
}
