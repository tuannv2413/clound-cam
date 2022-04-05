package org.thingsboard.server.dft.enduser.dto.box;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.entity.box.BoxEntity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
@NoArgsConstructor
public class BoxEditDto {
  private UUID id;

  @NotNull(message = "Đây là trường bắt buộc")
  @NotBlank(message = "Đây là trường bắt buộc")
  private String boxName;

  @NotNull(message = "Đây là trường bắt buộc")
  @NotBlank(message = "Đây là trường bắt buộc")
  @Size(min = 12, max = 12, message = "Serial Number có đội dài 12 ký tự")
  @Pattern(regexp = "[0-9]+", message = "Serial Number chỉ có thể là số")
  private String serialNumber;
//  private UUID locationId;

  public BoxEditDto(BoxEntity boxEntity) {
    this.id = boxEntity.getId();
    this.boxName = boxEntity.getBoxName();
    this.serialNumber = boxEntity.getSerialNumber();
  }
}
