package org.thingsboard.server.dft.mbgadmin.dto.mediaBox;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.thingsboard.server.dft.enduser.dto.BaseInfoDto;
import org.thingsboard.server.dft.mbgadmin.entity.mediaBox.MediaBoxEntity;

import java.util.UUID;

@Data
@NoArgsConstructor
public class MediaBoxDetailDto extends BaseInfoDto {
    private UUID id;
    private String originSerialNumber;
    private String serialNumber;
    private String partNumber;
    private String consignment;
    private String type;
    private String firmwareVersion;
    private UUID boxId;
    private UUID clTenantId;
    private String status;

    public MediaBoxDetailDto(MediaBoxEntity mediaBoxEntity) {
        super(mediaBoxEntity);
        this.id = mediaBoxEntity.getId();
        this.originSerialNumber = mediaBoxEntity.getOriginSerialNumber();
        this.partNumber = mediaBoxEntity.getPartNumber();
        this.serialNumber = mediaBoxEntity.getSerialNumber();
        this.consignment = mediaBoxEntity.getConsignment();
        this.type = mediaBoxEntity.getType();
        this.firmwareVersion = mediaBoxEntity.getFirmwareVersion();
        this.clTenantId = mediaBoxEntity.getTenantId();
        this.status = mediaBoxEntity.getStatus();
    }
}
