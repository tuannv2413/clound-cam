package org.thingsboard.server.dft.enduser.dao.cameraLive;

import org.thingsboard.server.dft.enduser.dto.cameraLive.CameraStreamViewerDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;

public interface CameraLiveDao {
  CameraStreamViewerDto saveViewerAndCamera(UUID cameraId, SecurityUser securityUser);

  void updateCameraStatusFalse(UUID cameraId);

  List<UUID> getAllCameraIsViewing();
}
