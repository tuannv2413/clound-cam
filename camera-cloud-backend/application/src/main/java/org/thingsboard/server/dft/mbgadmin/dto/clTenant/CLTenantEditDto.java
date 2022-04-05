package org.thingsboard.server.dft.mbgadmin.dto.clTenant;

import lombok.*;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CLTenantEditDto {
    private UUID id;
    private String code;
    private String name;
    private String type;
    private String email;
    private String phone;
    private String dayStartService;
    private String address;
    private String note;
    private Integer state;
    private List<UUID> mangeMediaBoxUuids;
    private List<UUID> serviceOptionUUID;
    private UUID createdOrUpdateBy;
}
