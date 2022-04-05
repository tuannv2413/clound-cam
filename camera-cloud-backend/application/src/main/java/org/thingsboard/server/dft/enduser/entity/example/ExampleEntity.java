package org.thingsboard.server.dft.enduser.entity.example;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.entity.BaseInfoEnity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@EqualsAndHashCode(callSuper=false)
@Data
@NoArgsConstructor
@Entity
@Table(name = "cl_example")
public class ExampleEntity extends BaseInfoEnity {
  @Id
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "name")
  private String name;

  @Column(name = "number")
  private int number;

  @Column(name = "checked")
  private boolean checked;

}
