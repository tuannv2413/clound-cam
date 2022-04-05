package org.thingsboard.server.dft.enduser.dto.cameraLive;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.entity.cameraLive.CameraStreamViewer;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CameraStreamViewerDto {
  private UUID cameraId;
  private UUID tbUserId;
  private boolean status;
  private long lastPing;

  public CameraStreamViewerDto(CameraStreamViewer cameraStreamViewer) {
    this.cameraId = cameraStreamViewer.getCameraId();
    this.tbUserId = cameraStreamViewer.getTbUserId();
    this.status = cameraStreamViewer.isStatus();
    this.lastPing = cameraStreamViewer.getLastPing();
  }
}
