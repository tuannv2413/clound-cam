package org.thingsboard.server.dft.mbgadmin.dto.clServiceOption;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.mbgadmin.entity.clServiceOption.CLServiceOptionEntity;

import java.util.UUID;

@NoArgsConstructor
@Data
public class CLServiceOptionDto {
    private UUID id;
    private String resolution;
    private Long price;

    public CLServiceOptionDto(CLServiceOptionEntity clServiceOptionEntity) {
        this.id = clServiceOptionEntity.getId();
        this.resolution = clServiceOptionEntity.getResolution();
        this.price = clServiceOptionEntity.getPrice();
    }
}
