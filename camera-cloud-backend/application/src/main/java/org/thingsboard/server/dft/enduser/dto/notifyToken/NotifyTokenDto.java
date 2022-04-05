package org.thingsboard.server.dft.enduser.dto.notifyToken;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
public class NotifyTokenDto {
    private UUID id;
    private UUID tbUserId;

    @NotNull
    private String notifyToken;

    private Long createdTime;
    private UUID createdby;
}
