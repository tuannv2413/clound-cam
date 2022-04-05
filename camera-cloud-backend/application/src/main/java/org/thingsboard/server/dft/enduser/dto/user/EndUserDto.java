package org.thingsboard.server.dft.enduser.dto.user;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dft.enduser.dto.BaseInfoDto;
import org.thingsboard.server.dft.enduser.entity.user.EndUserEntity;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Data
public class EndUserDto extends BaseInfoDto {
    private UUID id;
    private UUID tenantId;
    private String avatar;
    //Format 0912345671 - 912345671
    @Pattern(regexp = "^[+]?\\d*$", message = "Phone number format error")
    @Length(min = 6, max = 20)
    private String phone;
    private boolean active;
    @NotNull
    private Authority type;
    private boolean delete;
    private UUID customerId;
    @Length(max = 320)
    @Email
    private String email;
    private String searchText;
    @Length(min = 6,max = 30)
    private String password;
    private UUID tbUserId;
    private UUID createBy;
    private String createName;
    private String name;
    private long cameraNumber;

    public EndUserDto(EndUserEntity endUserEntity) {
        super(endUserEntity);
        this.id = endUserEntity.getId();
        this.tenantId = endUserEntity.getTenantId();
        this.name = endUserEntity.getName();
        this.phone = endUserEntity.getPhone();
        this.active = endUserEntity.isActive();
        this.type = endUserEntity.getType();
        this.delete = endUserEntity.isDelete();
        this.tbUserId = endUserEntity.getUserId();
        this.avatar = endUserEntity.getAvatar();
        this.email = endUserEntity.getEmail();
        this.createBy = endUserEntity.getCreatedBy();
    }

    public EndUserDto(EndUserEntity endUserEntity , UserEntity userEntity) {
        this.id = endUserEntity.getId();
        this.email = userEntity.getEmail();
        this.searchText = userEntity.getSearchText();
        this.tenantId = endUserEntity.getTenantId();
        this.phone = endUserEntity.getPhone();
        this.active = endUserEntity.isActive();
        this.type = endUserEntity.getType();
        this.delete = endUserEntity.isDelete();
        this.tbUserId = userEntity.getId();
        this.name = endUserEntity.getName();
        this.avatar = endUserEntity.getAvatar();
    }
}
