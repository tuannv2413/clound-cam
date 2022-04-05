package org.thingsboard.server.dft.enduser.service.videoHistory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thingsboard.server.dft.enduser.dao.videoHistory.VideoHistoryDao;
import org.thingsboard.server.dft.enduser.dto.videoHistory.VideoHistoryInfoDto;

import java.util.List;
import java.util.UUID;

@Service
public class VideoHistoryServiceImpl implements VideoHistoryService {

    private final VideoHistoryDao videoHistoryDao;

    @Autowired
    public VideoHistoryServiceImpl(VideoHistoryDao videoHistoryDao) {
        this.videoHistoryDao = videoHistoryDao;
    }

    @Override
    public List<VideoHistoryInfoDto> getListVideosInTimeRange(UUID camId, Long startTs, Long endTs) {
        return videoHistoryDao.getListVideosInTimeRange(camId, startTs, endTs);
    }
}
