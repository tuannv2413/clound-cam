package org.thingsboard.server.dft.enduser.dto.cameraGroup;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDetailDto;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CameraStreamGroup {
  private UUID groupId;
  private String groupName;
  private List<CameraDetailDto> listCamera;
}
