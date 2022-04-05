package org.thingsboard.server.dft.enduser.dto.camera;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCameraPermissionSaveDto {
    List<CustomerCameraPermissionDto> permissionList;
}
