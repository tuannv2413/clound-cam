package org.thingsboard.server.dft.enduser.repository.camera;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.enduser.entity.camera.CustomerCameraPermissionEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CameraPermissionRepository extends JpaRepository<CustomerCameraPermissionEntity, UUID> {

    @Query("select c from CustomerCameraPermissionEntity c where c.clCameraId = ?1 and c.clUserId = ?2")
    CustomerCameraPermissionEntity findByClCameraIdAndAndClUserId(UUID cameraId, UUID userId);

    @Query("select c from CustomerCameraPermissionEntity c where c.clUserId = ?1")
    List<CustomerCameraPermissionEntity> findByClUserId(UUID userId);

    boolean existsByClUserIdAndClCameraIdAndLiveTrue(UUID clUserId, UUID cameraId);

    boolean existsByClUserIdAndClCameraIdAndPtzTrue(UUID clUserId, UUID cameraId);

    boolean existsByClUserIdAndClCameraIdAndHistoryTrue(UUID clUserId, UUID cameraId);

}
