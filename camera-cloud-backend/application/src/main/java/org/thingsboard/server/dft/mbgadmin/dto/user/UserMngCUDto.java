package org.thingsboard.server.dft.mbgadmin.dto.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.thingsboard.server.common.data.security.Authority;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class UserMngCUDto {
    private String name;
    private String email;
    private String phone;
    private String office;
    private UUID roles;
    private String password;
//    private Authority type;
    private boolean active;
}
