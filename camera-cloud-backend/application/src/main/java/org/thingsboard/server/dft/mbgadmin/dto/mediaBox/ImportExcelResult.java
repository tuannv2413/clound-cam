package org.thingsboard.server.dft.mbgadmin.dto.mediaBox;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ImportExcelResult {
    private int total;
    private int success;
    private int fail;
    private List<Integer> listFail;
}
