package org.thingsboard.server.dft.mbgadmin.dto.clTenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GetIdDto {
    private UUID tenantId;
    private UUID userId;
    private UUID tbUserId;
}
