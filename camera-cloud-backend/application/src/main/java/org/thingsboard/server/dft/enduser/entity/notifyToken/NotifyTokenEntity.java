package org.thingsboard.server.dft.enduser.entity.notifyToken;

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
@Table(name = "cl_user_mobile_notify_token")
public class NotifyTokenEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tb_user_id", columnDefinition = "uuid")
    private UUID tbUserId;

    @Column(name = "mobile_notify_token")
    private String notifyToken;

    @Column(name = "created_time")
    private Long createdTime;

    @Column(name = "created_by")
    private UUID createdby;
}
