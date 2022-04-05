package org.thingsboard.server.dft.enduser.entity.clAlarmHistory;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "cl_alarm_history")
public class ClAlarmHistoryEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "alarmId", columnDefinition = "uuid")
    private UUID tbAlarmId;

    @Column(name = "device_alarm_id", columnDefinition = "uuid")
    private UUID tbDeviceId;

    @Column(name = "device_type")
    private String deviceType; // box or cam

    @Column(name = "type")
    private String type; // chdong, knoi, matknoi

    @Column(name = "viewed")
    private boolean viewed;

    @Column(name = "created_time")
    private Long createdTime;
}
