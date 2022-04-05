package org.thingsboard.server.dft.mbgadmin.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@Data
@MappedSuperclass
public class BaseEnity {
    @Column(name = "created_time")
    private long createdTime;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_time")
    private long updatedTime;

    @Column(name = "updated_by")
    private UUID updatedBy;
}
