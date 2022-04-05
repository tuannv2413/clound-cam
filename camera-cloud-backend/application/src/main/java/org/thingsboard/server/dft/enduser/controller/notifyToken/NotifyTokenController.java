package org.thingsboard.server.dft.enduser.controller.notifyToken;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.enduser.dto.notifyToken.NotifyTokenDto;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@TbCoreComponent
@RequestMapping("/api/notifyToken")
public class NotifyTokenController extends BaseController {

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @PostMapping
    @ApiOperation(value = "Lưu nofity token từ mobile", notes = "Chỉ cần điền trường notifyToken, các trường còn lại hệ thống tự điều chỉnh")
    public ResponseEntity<?> saveNotifyToken(@Valid @RequestBody NotifyTokenDto notifyTokenDto) throws ThingsboardException {
        try {
            notifyTokenDto.setNotifyToken(notifyTokenDto.getNotifyToken().trim());
            return ResponseEntity.ok(checkNotNull(notifyTokenService.saveNotifyToken(notifyTokenDto, getCurrentUser())));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
    @GetMapping
    @ApiOperation(value = "Lấy thông tin notify token của user hiện tại", notes = "Chỉ phục vụ testing - sẽ remove trong tương lai")
    public ResponseEntity<?> getCurrentUserNotifyTokens() throws ThingsboardException {
        try {
            UUID currentUserId = getCurrentUser().getUuidId();
            return ResponseEntity.ok(checkNotNull(notifyTokenService.findAllByTbUserId(currentUserId)));
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
