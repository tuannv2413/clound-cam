package org.thingsboard.server.dft.mbgadmin.dto.clTenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CLGroupServiceEditDto {
    private UUID id;
    private String name;
    private int maxDayStorage;
    private String note;
    private List<CLServiceOptionEditDto> clServiceOptionEditDtos;
}
