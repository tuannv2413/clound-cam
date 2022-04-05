package org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResponseDataDto {
    private Object data;
    private int totalPages;
    private int totalElements;
    private Boolean hasNext;

}
