package org.thingsboard.server.dft.enduser.entity.clAlarm;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dft.enduser.entity.BaseInfoEnity;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@Entity
@Table(name = "cl_alarm")
public class ClAlarmEntity extends BaseInfoEnity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "alarm_name")
    private String alarmName;

    @Column(name = "type")
    private String type; // ClAlarmType

    @Column(name = "via_notify")
    private boolean viaNotify;

    @Column(name = "via_sms")
    private boolean viaSms;

    @Column(name = "via_email")
    private boolean viaEmail;

    @Column(name = "active")
    private boolean active;

    @Column(name = "time_alarm_setting")
    private String timeAlarmSetting;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToMany
    @JoinTable(
            name = "cl_tbdevice_and_clalarm",
            joinColumns = @JoinColumn(name = "cl_alarm_id"),
            inverseJoinColumns = @JoinColumn(name = "tb_device_id")
    )
    private List<DeviceEntity> deviceEntities;

    @LazyCollection(LazyCollectionOption.FALSE)
    @ManyToMany
    @JoinTable(
            name = "cl_alarm_revicers",
            joinColumns = @JoinColumn(name = "cl_alarm_id"),
            inverseJoinColumns = @JoinColumn(name = "tb_user_id")
    )
    private List<UserEntity> alarmReceivers;
}
