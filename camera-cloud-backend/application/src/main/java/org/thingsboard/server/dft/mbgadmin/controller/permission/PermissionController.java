package org.thingsboard.server.dft.mbgadmin.controller.permission;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.mbgadmin.dto.permission.PermissionGetDto;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.annotation.security.PermitAll;
import java.util.List;

@RestController
@TbCoreComponent
@RequestMapping("/api/permission")
public class PermissionController extends BaseController {
    @PermitAll
    @GetMapping
    @ResponseBody
    public ResponseEntity<?> getAll(){
        try {
            List<PermissionGetDto> permissionList =
                    permissionService.getAll();
            return new ResponseEntity<>(checkNotNull(permissionList), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }
}
