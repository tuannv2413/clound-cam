package org.thingsboard.server.dft.mbgadmin.dto.clGroupService;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.BaseInfoDto;
import org.thingsboard.server.dft.mbgadmin.entity.clGroupService.CLGroupServiceEntity;

import java.util.UUID;

@NoArgsConstructor
@Data
public class CLGroupServiceDto {
    private UUID id;
    private String name;
    private int maxDayStorage;
    private String note;
    private Boolean active;

    public CLGroupServiceDto(CLGroupServiceEntity clGroupServiceEntity) {
        this.id = clGroupServiceEntity.getId();
        this.name = clGroupServiceEntity.getName();
        this.maxDayStorage = clGroupServiceEntity.getMaxDayStorage();
        this.note = clGroupServiceEntity.getNote();
        this.active = clGroupServiceEntity.getActive();
    }
}
