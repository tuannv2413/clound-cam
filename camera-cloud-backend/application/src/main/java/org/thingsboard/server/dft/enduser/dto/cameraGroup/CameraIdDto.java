package org.thingsboard.server.dft.enduser.dto.cameraGroup;

import lombok.*;

import java.util.UUID;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CameraIdDto {
    private UUID cameraId;
}
