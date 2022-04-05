package org.thingsboard.server.dft.mbgadmin.repository.clTenant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.mbgadmin.entity.clServiceOption.CLServiceOptionEntity;
import org.thingsboard.server.dft.mbgadmin.entity.clTenant.CLTenantEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CLTenantRepository extends JpaRepository<CLTenantEntity, UUID> {
    @Query(value = "SELECT DISTINCT cltenant.*  FROM cl_tenant cltenant " +
            "LEFT JOIN cl_service_option_and_cltenant spat ON cltenant.id = spat.cl_tenant_id " +
            "LEFT JOIN cl_service_option sp ON sp.id = spat.service_option_id " +
            "LEFT JOIN cl_group_service clgroupservice ON clgroupservice.id = sp.group_service_id " +
            "LEFT JOIN cl_user cluser ON cltenant.cl_user_id = cluser.id " +
            "LEFT JOIN tenant ON cluser.tenant_id = tenant.id " +
            "WHERE " +
            "(LOWER(cltenant.code) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "OR LOWER(cluser.name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "OR LOWER(cluser.email) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "OR cluser.phone LIKE (CONCAT('%', :searchText, '%'))) " +
            "AND (:typeCustomer = '' OR cltenant.type = :typeCustomer) " +
            "AND (cluser.type <> '0') " +
            "AND (cluser.tenant_id <> CAST('13814000-1dd2-11b2-8080-808080808080' AS UUID)) " +
            "AND (:groupService IS NULL OR clgroupservice.id = CAST(CAST(:groupService AS CHARACTER VARYING) AS UUID)) " +
            "AND (:state = '0' OR tenant.state = :state) " +
            "AND ((:startDate = 0 AND :endDate = 0) OR (cltenant.day_start_service >= :startDate AND cltenant.day_start_service <= :endDate))", nativeQuery = true)
    Page<CLTenantEntity> findALLAndSearch(Pageable pageable, @Param("typeCustomer") String typeCustomer, @Param("groupService") UUID groupService, @Param("state") String state, @Param("startDate") long startDate, @Param("endDate") long endDate, @Param("searchText") String searchText);

    Optional<CLTenantEntity> findByCode(String code);

    Boolean existsByCode(String code);

    @Query(value = "SELECT a.* FROM cl_tenant a ORDER BY CAST(a.code AS INTEGER) DESC", nativeQuery = true)
    List<CLTenantEntity> findCodeMax();

    @Query(value = "SELECT count(t) > 0 FROM cl_tenant t where t.cl_user_id = ?1", nativeQuery = true)
    boolean isTenantUser(UUID clUserId);

}
