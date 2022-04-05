package org.thingsboard.server.dft.enduser.dao.videoHistory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thingsboard.server.dft.enduser.dto.videoHistory.VideoHistoryInfoDto;
import org.thingsboard.server.dft.enduser.entity.videoHistory.VideoHistoryEntity;
import org.thingsboard.server.dft.enduser.repository.videoHistory.VideoHistoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class VideoHistoryDaoImpl implements VideoHistoryDao {

    private final VideoHistoryRepository videoHistoryRepository;

    @Autowired
    public VideoHistoryDaoImpl(VideoHistoryRepository videoHistoryRepository) {
        this.videoHistoryRepository = videoHistoryRepository;
    }


    @Override
    public List<VideoHistoryInfoDto> getListVideosInTimeRange(UUID camId, Long startTs, Long endTs) {
        List<VideoHistoryInfoDto> result = new ArrayList<>();
        List<VideoHistoryEntity> videoHistoryEntities;
        videoHistoryEntities = videoHistoryRepository.findAllByTimeRange(camId, startTs, endTs);
        if (videoHistoryEntities != null && videoHistoryEntities.size() != 0) {
            videoHistoryEntities.forEach(x -> {
                result.add(castVideoHistoryEntityToVideoHistoryInfoDto(x));
            });
        }
        return result;
    }

    private VideoHistoryInfoDto castVideoHistoryEntityToVideoHistoryInfoDto(VideoHistoryEntity videoHistoryEntity) {
        VideoHistoryInfoDto result = new VideoHistoryInfoDto();
        result.setId(videoHistoryEntity.getId());
        result.setBoxId(videoHistoryEntity.getBoxId());
        result.setCameraId(videoHistoryEntity.getCameraId());
        result.setVideoUrl(videoHistoryEntity.getVideoUrl());
        result.setSize(videoHistoryEntity.getSize());
        result.setStartVideoTime(videoHistoryEntity.getStartVideoTime());
        result.setEndVideoTime(videoHistoryEntity.getEndVideoTime());
        result.setCreatedTime(videoHistoryEntity.getCreatedTime());
        return result;
    }
}
