package org.thingsboard.server.dft.mbgadmin.dto.clGroupService;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.BaseInfoDto;
import org.thingsboard.server.dft.mbgadmin.dto.clServiceOption.CLServiceOptionDto;
import org.thingsboard.server.dft.mbgadmin.entity.clGroupService.CLGroupServiceEntity;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Data
public class CLGroupServiceDetailDto extends BaseInfoDto {
    private UUID id;
    private String name;
    private int maxDayStorage;
    private String note;
    private Boolean active;
    private List<CLServiceOptionDto> clServiceOptionDtos;

    public CLGroupServiceDetailDto(CLGroupServiceEntity clGroupServiceEntity, List<CLServiceOptionDto> clServiceOptionDtos) {
        super(clGroupServiceEntity);
        this.id = clGroupServiceEntity.getId();
        this.name = clGroupServiceEntity.getName();
        this.maxDayStorage = clGroupServiceEntity.getMaxDayStorage();
        this.note = clGroupServiceEntity.getNote();
        this.active = clGroupServiceEntity.getActive();
        this.clServiceOptionDtos = clServiceOptionDtos;
    }
}
