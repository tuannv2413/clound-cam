package org.thingsboard.server.dft.mbgadmin.dto.clTenant;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.BaseInfoDto;
import org.thingsboard.server.dft.mbgadmin.entity.clTenant.CLTenantEntity;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CLTenantDto {
    private UUID id;
    private String code;
    private String name;
    private String type;
    private String state;
    private String dayStartService;
    private String address;
    private String note;
    private String email;
    private String phone;
    private int quantityBox;
    private String groupService;
    private long totalPrice;

    public CLTenantDto(CLTenantEntity clTenantEntity, String dayStartService, String groupService, long totalPrice) {
        this.id = clTenantEntity.getId();
        this.code = clTenantEntity.getCode();
        this.name = clTenantEntity.getClUserEntity().getName();
        this.type = clTenantEntity.getType();
        this.state = clTenantEntity.getClUserEntity().getTenantEntityQL().getState();
        this.dayStartService = dayStartService;
        this.address = clTenantEntity.getAddress();
        this.note = clTenantEntity.getNote();
        this.email = clTenantEntity.getClUserEntity().getEmail();
        this.phone = clTenantEntity.getClUserEntity().getPhone();
        this.quantityBox = clTenantEntity.getClMangeMediaBoxEntities().size();
        this.groupService = groupService;
        this.totalPrice = totalPrice;
    }
}
