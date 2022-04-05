package org.thingsboard.server.dft.enduser.dto.videoHistory;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class VideoHistoryInfoDto {
    private UUID id;
    private UUID boxId;
    private UUID cameraId; //
    private String videoUrl;
    private Long size;
    private Long startVideoTime; //
    private Long endVideoTime; //
    private Long createdTime;
}
