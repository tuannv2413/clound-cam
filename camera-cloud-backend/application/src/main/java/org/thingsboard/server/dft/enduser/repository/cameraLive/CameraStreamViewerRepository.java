package org.thingsboard.server.dft.enduser.repository.cameraLive;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.dft.enduser.entity.cameraLive.CameraStreamViewer;

import java.util.List;
import java.util.UUID;

@Repository
public interface CameraStreamViewerRepository extends JpaRepository<CameraStreamViewer, UUID> {

  CameraStreamViewer findByCameraIdAndTbUserId(UUID cameraId, UUID tbUserId);

  boolean existsByCameraIdAndStatus(UUID cameraId, boolean status);

  @Query("select distinct cv.cameraId from CameraStreamViewer cv where cv.status = true")
  List<UUID> getAllStreamViewing();

  @Transactional
  @Modifying
  @Query("update CameraStreamViewer cv set cv.status = false where cv.cameraId = :cameraId and cv.status = true")
  void updateStatusByCameraId(@Param("cameraId") UUID cameraId);

}
