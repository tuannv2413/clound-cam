package org.thingsboard.server.dft.enduser.dto.example;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.BaseInfoDto;
import org.thingsboard.server.dft.enduser.entity.example.ExampleEntity;

import java.util.UUID;

@Data
@NoArgsConstructor
public class ExampleDto extends BaseInfoDto {
  private UUID id;
  private String name;
  private int number;
  private boolean checked;

  public ExampleDto(ExampleEntity exampleEntity) {
    super(exampleEntity);
    this.id = exampleEntity.getId();
    this.name = exampleEntity.getName();
    this.number = exampleEntity.getNumber();
    this.checked = exampleEntity.isChecked();
  }
}
