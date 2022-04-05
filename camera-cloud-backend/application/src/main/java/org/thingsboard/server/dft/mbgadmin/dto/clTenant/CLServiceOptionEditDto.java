package org.thingsboard.server.dft.mbgadmin.dto.clTenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CLServiceOptionEditDto {
    private UUID id;
    private String resolution;
    private Long price;
}
