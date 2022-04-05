package org.thingsboard.server.dft.enduser.controller.notificationSetting;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.enduser.dto.notificationSetting.NotificationSettingDto;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.validation.Valid;

@Slf4j
@RestController
@TbCoreComponent
@RequestMapping("/api/sys-admin/notificationSetting")
public class NotificationSettingController extends BaseController {

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @PostMapping
    @ApiOperation(value = "cập nhật setting cho firebase notification", notes = "")
    public ResponseEntity<?> settingNotificationServer(@Valid @RequestBody NotificationSettingDto notificationSettingDto) throws ThingsboardException {
        try {
            return ResponseEntity.ok(checkNotNull(notificationSettingService.save(notificationSettingDto)));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PreAuthorize("hasAnyAuthority('SYS_ADMIN')")
    @GetMapping
    @ApiOperation(value = "Lấy thông tin setting firebase notification", notes = "")
    public ResponseEntity<?> getNotificationServerSetting() throws ThingsboardException {
        try {
            return ResponseEntity.ok(checkNotNull(notificationSettingService.getNotificationSetting()));
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
