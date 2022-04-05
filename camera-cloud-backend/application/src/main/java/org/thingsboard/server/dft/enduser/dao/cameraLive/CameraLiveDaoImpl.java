package org.thingsboard.server.dft.enduser.dao.cameraLive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thingsboard.server.dft.enduser.dto.cameraLive.CameraStreamViewerDto;
import org.thingsboard.server.dft.enduser.entity.cameraLive.CameraStreamViewer;
import org.thingsboard.server.dft.enduser.repository.cameraLive.CameraStreamViewerRepository;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class CameraLiveDaoImpl implements CameraLiveDao {

  private final CameraStreamViewerRepository cameraStreamViewerRepository;

  @Autowired
  public CameraLiveDaoImpl(CameraStreamViewerRepository cameraStreamViewerRepository) {
    this.cameraStreamViewerRepository = cameraStreamViewerRepository;
  }

  @Override
  public CameraStreamViewerDto saveViewerAndCamera(UUID cameraId, SecurityUser securityUser) {
    CameraStreamViewer cameraStreamViewer = cameraStreamViewerRepository
        .findByCameraIdAndTbUserId(cameraId, securityUser.getUuidId());
    if (cameraStreamViewer == null) {
      cameraStreamViewer = new CameraStreamViewer();
      cameraStreamViewer.setId(UUID.randomUUID());
    }
    cameraStreamViewer.setStatus(true);
    cameraStreamViewer.setLastPing(new Date().getTime());
    cameraStreamViewer.setCameraId(cameraId);
    cameraStreamViewer.setTbUserId(securityUser.getUuidId());
    return new CameraStreamViewerDto(cameraStreamViewerRepository.save(cameraStreamViewer));
  }

  @Override
  public void updateCameraStatusFalse(UUID cameraId) {
    cameraStreamViewerRepository.updateStatusByCameraId(cameraId);
  }

  @Override
  public List<UUID> getAllCameraIsViewing() {
    return cameraStreamViewerRepository.getAllStreamViewing();
  }


}
