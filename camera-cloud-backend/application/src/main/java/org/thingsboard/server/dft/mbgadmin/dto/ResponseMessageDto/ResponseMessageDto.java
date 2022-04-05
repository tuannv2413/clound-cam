package org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
public class ResponseMessageDto {
    private Integer status;
    private String message;
    private Integer errorCode;
    private Timestamp timestamp;
}
