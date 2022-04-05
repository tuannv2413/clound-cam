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
public class CLBoxEditDto {
    private UUID id;
    private String serialNumber;
    private String partNumber;
    private String consignment;
    private String type;
    private String firmwareVersion;
    private String status;
}
