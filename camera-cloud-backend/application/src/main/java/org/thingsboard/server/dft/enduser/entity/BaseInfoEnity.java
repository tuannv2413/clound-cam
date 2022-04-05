package org.thingsboard.server.dft.enduser.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseInfoEnity {

  @Column(name = "created_time")
  private long createdTime;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "updated_time")
  private long updatedTime;

  @Column(name = "updated_by")
  private UUID updatedBy;

}
