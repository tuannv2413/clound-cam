package org.thingsboard.server.dft.mbgadmin.dto.clTenant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CLTenantEditResponse {
    int code;
    CLTenantDetailDto data;
}
