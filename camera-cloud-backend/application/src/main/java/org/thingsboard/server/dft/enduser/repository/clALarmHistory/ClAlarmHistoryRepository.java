package org.thingsboard.server.dft.enduser.repository.clALarmHistory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.enduser.entity.clAlarmHistory.ClAlarmHistoryEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ClAlarmHistoryRepository extends JpaRepository<ClAlarmHistoryEntity, UUID> {

    @Query(value = "SELECT ah FROM ClAlarmHistoryEntity ah " +
            "WHERE ah.tenantId = :tenantId " +
            " AND ( :viewed IS NULL OR ah.viewed = :viewed )" +
            " AND ( :startTs IS NULL OR ah.createdTime >= :startTs )" +
            " AND ( :endTs IS NULL OR ah.createdTime <= :endTs )")
    Page<ClAlarmHistoryEntity> findAllWithPaging(
            @Param("tenantId") UUID tenantId,
            @Param("viewed") Boolean viewed,
            @Param("startTs") Long startTs,
            @Param("endTs") Long endTs,
            Pageable pageable
    );

    @Query(value = "SELECT ah FROM ClAlarmHistoryEntity ah " +
            "WHERE ah.tenantId = :tenantId " +
            " AND ( :viewed IS NULL OR ah.viewed = :viewed )" +
            " AND ah.type = :type" +
            " AND ah.tbDeviceId = :tbDeviceId" +
            " AND ( :startTs IS NULL OR ah.createdTime >= :startTs )" +
            " AND ( :endTs IS NULL OR ah.createdTime <= :endTs )")
    Page<ClAlarmHistoryEntity> findAllWithPaging(
            @Param("tenantId") UUID tenantId,
            @Param("viewed") Boolean viewed,
            @Param("type") String type,
            @Param("tbDeviceId") UUID boxTbDeviceId,
            @Param("startTs") Long startTs,
            @Param("endTs") Long endTs,
            Pageable pageable
    );

    @Query(value = "SELECT ah FROM ClAlarmHistoryEntity ah " +
            "WHERE ah.tenantId = :tenantId" +
            " AND ( :viewed IS NULL OR ah.viewed = :viewed )" +
            " AND ah.type = :type " +
            " AND ( :startTs IS NULL OR ah.createdTime >= :startTs )" +
            " AND ( :endTs IS NULL OR ah.createdTime <= :endTs )")
    Page<ClAlarmHistoryEntity> findAllWithPaging(
            @Param("tenantId") UUID tenantId,
            @Param("viewed") Boolean viewed,
            @Param("type") String type,
            @Param("startTs") Long startTs,
            @Param("endTs") Long endTs,
            Pageable pageable
    );

    @Query(value = "SELECT ah FROM ClAlarmHistoryEntity ah " +
            "WHERE ah.tenantId = :tenantId" +
            " AND ( :viewed IS NULL OR ah.viewed = :viewed )" +
            " AND ah.tbDeviceId = :tbDeviceId " +
            " AND ( :startTs IS NULL OR ah.createdTime >= :startTs )" +
            " AND ( :endTs IS NULL OR ah.createdTime <= :endTs )")
    Page<ClAlarmHistoryEntity> findAllWithPaging(
            @Param("tenantId") UUID tenantId,
            @Param("viewed") Boolean viewed,
            @Param("tbDeviceId") UUID boxTbDeviceId,
            @Param("startTs") Long startTs,
            @Param("endTs") Long endTs,
            Pageable pageable
    );

    int countAllByTenantIdAndViewed(UUID tenantId, boolean viewed);

    List<ClAlarmHistoryEntity> findAllByTenantIdAndViewed(UUID tenantId, boolean view);

    List<ClAlarmHistoryEntity> findAllByTenantIdAndIdIn(UUID tenantId, Set<UUID> alarmHistoryIds);

}
