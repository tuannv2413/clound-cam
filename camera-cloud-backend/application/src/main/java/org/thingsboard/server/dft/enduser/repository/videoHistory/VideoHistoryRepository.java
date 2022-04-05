package org.thingsboard.server.dft.enduser.repository.videoHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.enduser.entity.videoHistory.VideoHistoryEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface VideoHistoryRepository extends JpaRepository<VideoHistoryEntity, UUID> {

    @Query(value = "SELECT t FROM VideoHistoryEntity t " +
            "WHERE t.cameraId = :camId " +
            "AND ((t.startVideoTime BETWEEN :startTs AND :endTs ) " +
            "OR (t.endVideoTime BETWEEN :startTs AND :endTs)) " +
            "ORDER BY t.startVideoTime ASC")
    List<VideoHistoryEntity> findAllByTimeRange(
            @Param("camId") UUID camId,
            @Param("startTs") Long startTs,
            @Param("endTs") Long endTs
    );
}
