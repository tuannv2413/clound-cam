package org.thingsboard.server.dft.enduser.repository.camera;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.dft.enduser.entity.camera.CameraRecordSettingEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CameraRecordSettingRepository extends JpaRepository<CameraRecordSettingEntity, UUID> {

  List<CameraRecordSettingEntity> findAllByTenantId(UUID tenantId);

  CameraRecordSettingEntity findByCameraIdAndTenantId(UUID cameraId, UUID tenantId);

  boolean existsByNameAndTenantId(String name, UUID tenantId);

  boolean existsByNameAndTenantIdAndIdNot(String name, UUID tenantId, UUID id);

  @Modifying
  @Transactional
  void deleteByCameraIdAndTenantId(UUID cameraId, UUID tenantId);

  @Modifying
  @Transactional
  @Query("DELETE FROM CameraRecordSettingEntity cs WHERE cs.cameraId IN (SELECT c.id FROM CameraEntity c WHERE c.boxEntity.id = :boxId) " +
      "AND cs.tenantId = :tenantId")
  void deleteByBoxIdAndTenantId(@Param("boxId") UUID boxId,@Param("tenantId") UUID tenantId);
}
