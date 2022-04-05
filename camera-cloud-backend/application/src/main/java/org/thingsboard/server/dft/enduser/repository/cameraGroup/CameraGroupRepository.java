package org.thingsboard.server.dft.enduser.repository.cameraGroup;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.enduser.entity.cameraGroup.CameraGroupEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CameraGroupRepository extends JpaRepository<CameraGroupEntity, UUID> {

    boolean existsByCameraGroupName(String cameraGroupName);

    @Query("select ex from CameraGroupEntity ex where ex.tenantId =:tenantId and LOWER(ex.cameraGroupName) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<CameraGroupEntity> findAllByName(@Param("tenantId") UUID tenantId, Pageable pageable, @Param("searchText") String searchText);

    CameraGroupEntity findByTenantIdAndId(UUID tenantId, UUID groupId);

    Optional<CameraGroupEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    List<CameraGroupEntity> findAllByTenantId(UUID tenantId);

    List<CameraGroupEntity> findAllByTenantId(Sort sort, UUID tenantId);


    @Query("select c from CameraGroupEntity c where c.isDefault = true")
    List<CameraGroupEntity> findByIsDefaultTrue();

    Boolean existsByIsDefaultTrue();
    
}
