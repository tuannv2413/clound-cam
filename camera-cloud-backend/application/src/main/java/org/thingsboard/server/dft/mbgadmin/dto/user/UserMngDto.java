package org.thingsboard.server.dft.mbgadmin.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.dft.mbgadmin.dto.BaseDto;
import org.thingsboard.server.dft.mbgadmin.entity.user.UserMngEntity;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class UserMngDto extends BaseDto {
    private UUID id;
    private UUID tenantId;
    private String name;
    private String phone;
    private String email;
    private String office;
    private String avatar;
    private boolean active;
    private Authority type;

    private UUID createdBy;
    private UUID updatedBy;
    private UUID tbUserId;

    private String searchText;

    public UserMngDto(UserMngEntity userMngEntity) {
        super(userMngEntity);
        this.id = userMngEntity.getId();
        this.name = userMngEntity.getName();
        this.tenantId = userMngEntity.getTenantId();
        this.phone = userMngEntity.getPhone();
        this.email = userMngEntity.getEmail();
        this.office = userMngEntity.getOffice();
        this.avatar = userMngEntity.getAvatar();
        this.active = userMngEntity.isActive();
        this.type = userMngEntity.getType();
        this.createdBy = userMngEntity.getCreatedBy();
        this.updatedBy = userMngEntity.getUpdatedBy();
        this.tbUserId = userMngEntity.getTbUserId();
    }
}