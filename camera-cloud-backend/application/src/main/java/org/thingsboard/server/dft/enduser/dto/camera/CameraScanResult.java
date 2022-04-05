package org.thingsboard.server.dft.enduser.dto.camera;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CameraScanResult {
  private String deviceName;
  private String ipv4;
  private int onvifPort;
  private String resolution;
  private String protocol;
  private boolean hasAuth;
  private UUID boxId;
  private String boxName;
  private String onvifUrl;
}
