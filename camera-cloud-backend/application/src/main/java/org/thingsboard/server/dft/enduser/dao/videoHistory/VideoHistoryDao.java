package org.thingsboard.server.dft.enduser.dao.videoHistory;

import org.thingsboard.server.dft.enduser.dto.videoHistory.VideoHistoryInfoDto;

import java.util.List;
import java.util.UUID;

public interface VideoHistoryDao {
    List<VideoHistoryInfoDto> getListVideosInTimeRange(UUID camId, Long startTs, Long endTs);
}
