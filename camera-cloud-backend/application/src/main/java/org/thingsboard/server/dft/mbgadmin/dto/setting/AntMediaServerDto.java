package org.thingsboard.server.dft.mbgadmin.dto.setting;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AntMediaServerDto {
  private String httpUrl;
  private String webSocketUrl;
  private String license;
}
