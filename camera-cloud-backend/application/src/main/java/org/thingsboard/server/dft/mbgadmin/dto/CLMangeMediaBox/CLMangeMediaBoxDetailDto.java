package org.thingsboard.server.dft.mbgadmin.dto.CLMangeMediaBox;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.BaseInfoDto;
import org.thingsboard.server.dft.mbgadmin.entity.clMangeMediaBox.CLMangeMediaBoxEntity;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CLMangeMediaBoxDetailDto {
    private UUID id;
    private String serialNumber;
    private String partNumber;
    private String consignment;
    private String type;
    private String firmwareVersion;
    private String status;

    public CLMangeMediaBoxDetailDto(CLMangeMediaBoxEntity clMangeMediaBoxEntity) {
        this.id = clMangeMediaBoxEntity.getId();
        this.serialNumber = clMangeMediaBoxEntity.getSerialNumber();
        this.partNumber = clMangeMediaBoxEntity.getPartNumber();
        this.consignment = clMangeMediaBoxEntity.getConsignment();
        this.type = clMangeMediaBoxEntity.getType();
        this.firmwareVersion = clMangeMediaBoxEntity.getFirmwareVersion();
        this.status = clMangeMediaBoxEntity.getStatus();
    }
}
