package org.thingsboard.server.dft.mbgadmin.dto.clTenant;

import lombok.*;
import org.thingsboard.server.dft.enduser.dto.BaseInfoDto;
import org.thingsboard.server.dft.mbgadmin.dto.CLMangeMediaBox.CLMangeMediaBoxDetailDto;
import org.thingsboard.server.dft.mbgadmin.dto.clGroupService.CLGroupServiceDto;
import org.thingsboard.server.dft.mbgadmin.dto.clServiceOption.CLServiceOptionDto;
import org.thingsboard.server.dft.mbgadmin.dto.mediaBox.MediaBoxEditDto;
import org.thingsboard.server.dft.mbgadmin.entity.clTenant.CLTenantEntity;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Data
public class CLTenantDetailDto {
    private UUID id;
    private String code;
    private String name;
    private Boolean active;
    private String type;
    private String state;
    private String email;
    private String phone;
    private String dayStartService;
    private String address;
    private String note;
    private List<CLMangeMediaBoxDetailDto> mediaBoxDto;
    private CLGroupServiceDto groupServiceDto;
    private List<CLServiceOptionDto> serviceOption;


    public CLTenantDetailDto(CLTenantEntity clTenantEntity, List<CLMangeMediaBoxDetailDto> mediaBoxDto,CLGroupServiceDto groupServiceDto, List<CLServiceOptionDto> serviceOption, String dayStartService) {
        this.id = clTenantEntity.getId();
        this.code = clTenantEntity.getCode();
        this.name = clTenantEntity.getClUserEntity().getName();
        this.active = clTenantEntity.getClUserEntity().getActive();
        this.type = clTenantEntity.getType();
        this.state = clTenantEntity.getClUserEntity().getTenantEntityQL().getState();
        this.email = clTenantEntity.getClUserEntity().getEmail();
        this.phone = clTenantEntity.getClUserEntity().getPhone();
        this.dayStartService = dayStartService;
        this.address = clTenantEntity.getAddress();
        this.note = clTenantEntity.getNote();
        this.mediaBoxDto = mediaBoxDto;
        this.groupServiceDto = groupServiceDto;
        this.serviceOption = serviceOption;
    }
}
