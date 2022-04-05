package org.thingsboard.server.dft.enduser.service.videoHistory;

import org.thingsboard.server.dft.enduser.dto.videoHistory.VideoHistoryInfoDto;

import java.util.List;
import java.util.UUID;

public interface VideoHistoryService {
    List<VideoHistoryInfoDto> getListVideosInTimeRange(UUID camId, Long startTs, Long endTs);
}
