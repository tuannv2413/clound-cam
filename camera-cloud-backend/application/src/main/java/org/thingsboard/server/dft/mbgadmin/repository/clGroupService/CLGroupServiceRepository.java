package org.thingsboard.server.dft.mbgadmin.repository.clGroupService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.mbgadmin.entity.clGroupService.CLGroupServiceEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CLGroupServiceRepository extends JpaRepository<CLGroupServiceEntity, UUID> {
    @Query("SELECT DISTINCT a FROM CLGroupServiceEntity a JOIN a.clServiceOptionEntities b WHERE " +
            "(LOWER(a.name) LIKE LOWER(CONCAT('%', :textSearch, '%')) " +
            "OR LOWER(a.note) LIKE LOWER(CONCAT('%', :textSearch, '%'))) " +
            "AND (:maxDayStorage = 0 OR a.maxDayStorage = :maxDayStorage) " +
            "AND (:active is null OR a.active = :active)")
    Page<CLGroupServiceEntity> findALLAndSearch(Pageable pageable, @Param("maxDayStorage") Integer maxDayStorage, @Param("active") Boolean active, @Param("textSearch") String textSearch);

    @Query("SELECT a FROM CLGroupServiceEntity a JOIN a.clServiceOptionEntities b WHERE (:resolution is null OR b.resolution = :resolution) AND a.active = true")
    Page<CLGroupServiceEntity> findResolution(Pageable pageable, @Param("resolution") String resolution);

    Boolean existsByName(String name);

    Boolean existsByMaxDayStorage(Integer maxDayStorage);

    @Query("SELECT a FROM CLGroupServiceEntity a ORDER BY a.maxDayStorage ASC ")
    List<CLGroupServiceEntity> findOrderByMaxDayStorageAsc();

    @Modifying
    @Query(value = "DELETE FROM cl_service_option_and_cltenant a WHERE a.service_option_id = :serviceOptionId", nativeQuery = true)
    void deleteServiceOptionAndTenant(@Param("serviceOptionId") UUID serviceOptionId);
}
