package org.thingsboard.server.dft.enduser.repository.clAlarm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.enduser.entity.clAlarm.ClAlarmEntity;

import java.util.UUID;

@Repository
public interface ClAlarmRepository extends JpaRepository<ClAlarmEntity, UUID> {

    @Query(value = "SELECT c FROM ClAlarmEntity c WHERE c.tenantId = :tenantId " +
            "AND LOWER(c.alarmName) LIKE LOWER(CONCAT('%', :textSearch, '%')) " +
            "AND c.type = (CASE WHEN :clAlarmType IS NULL THEN c.type ELSE :clAlarmType END) ")
    Page<ClAlarmEntity> findAllWithPaging
            (@Param("tenantId") UUID tenantId,
             @Param("textSearch") String textSearch,
             @Param("clAlarmType") String type,
             Pageable pageable);
}
