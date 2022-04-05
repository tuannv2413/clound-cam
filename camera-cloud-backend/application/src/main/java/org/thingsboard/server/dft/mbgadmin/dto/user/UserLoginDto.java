package org.thingsboard.server.dft.mbgadmin.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.dft.mbgadmin.dto.role.RoleDto;
import org.thingsboard.server.dft.mbgadmin.entity.user.UserMngEntity;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class UserLoginDto extends UserMngDto {
    private List<RoleDto> roles;
    private List<String> permissionNames;
    private String authority;

    public UserLoginDto(UserMngEntity userMngEntity, List<RoleDto> roles, List<String> permissionNames) {
        super(userMngEntity);
        this.roles = roles;
        this.permissionNames = permissionNames;
    }

    public UserLoginDto (User user){
        this.setId(user.getUuidId());
        this.setEmail(user.getEmail());
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String fullName = "";
        if (firstName.equals(lastName)) {
            fullName = firstName;
        } else
            fullName = firstName + " " + lastName;
        this.setName(fullName.trim());
        this.setTenantId(user.getTenantId().getId());
        this.setSearchText(user.getSearchText());
        if (user.getAuthority() != null)
            this.setAuthority(user.getAuthority().name());
    }
}
