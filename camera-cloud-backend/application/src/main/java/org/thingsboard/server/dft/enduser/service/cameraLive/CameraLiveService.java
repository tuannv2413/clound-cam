package org.thingsboard.server.dft.enduser.service.cameraLive;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.dft.enduser.dto.cameraLive.CameraStreamViewerDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.UUID;

public interface CameraLiveService {
  CameraStreamViewerDto saveToStreamViewer(UUID cameraId, SecurityUser securityUser);

  boolean setCameraOnline(UUID cameraId, SecurityUser securityUser) throws ThingsboardException, JSONException, JsonProcessingException;

  void checkStopStreamNoView() throws JSONException, JsonProcessingException, ThingsboardException;
}
