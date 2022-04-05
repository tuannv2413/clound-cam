package org.thingsboard.server.dft.mbgadmin.controller.setting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.AdminSettings;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.AdminSettingsId;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dao.settings.AdminSettingsService;
import org.thingsboard.server.dft.mbgadmin.dto.setting.AntMediaServerDto;
import org.thingsboard.server.dft.mbgadmin.dto.setting.ServerUrlDto;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import javax.annotation.security.PermitAll;
import java.util.Date;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api/cloud-setting")
public class CloudSettingController extends BaseController {

  @Autowired
  AdminSettingsService adminSettingsService;

  private final ObjectMapper mapper = new ObjectMapper();

  @PermitAll
  @PostMapping("live-stream")
  @ResponseBody
  @ApiOperation(value = "setting server live streaming camera")
  public ResponseEntity<?> createOrUpdateUrlStreamServer(@RequestBody AntMediaServerDto antMediaServerDto) throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      AdminSettings adminSettings = adminSettingsService.findAdminSettingsByKey(securityUser.getTenantId(), "live-stream");
      if (adminSettings == null) {
        adminSettings = new AdminSettings();
        adminSettings.setId(new AdminSettingsId(UUID.randomUUID()));
        adminSettings.setCreatedTime(new Date().getTime());
      }
      adminSettings.setKey("live-stream");
      JsonNode dataJson = mapper.convertValue(antMediaServerDto, JsonNode.class);
      adminSettings.setJsonValue(dataJson);
      return new ResponseEntity<>(checkNotNull(adminSettingsService
          .saveAdminSettings(securityUser.getTenantId(), adminSettings)), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PermitAll
  @GetMapping("live-stream")
  @ResponseBody
  @ApiOperation(value = "get url server live streaming camera")
  public ResponseEntity<?> getUrlStreamServer() throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      AdminSettings adminSettings = adminSettingsService.findAdminSettingsByKey(securityUser.getTenantId(), "live-stream");
      return new ResponseEntity<>(checkNotNull(adminSettings.getJsonValue()), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PermitAll
  @PostMapping("web-enduser")
  @ResponseBody
  @ApiOperation(value = "setting server chạy web end user")
  public ResponseEntity<?> createOrUpdateUrlWebEndUser(@RequestBody ServerUrlDto serverUrlDto) throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      AdminSettings adminSettings = adminSettingsService.findAdminSettingsByKey(securityUser.getTenantId(), "web-enduser");
      if (adminSettings == null) {
        adminSettings = new AdminSettings();
        adminSettings.setId(new AdminSettingsId(UUID.randomUUID()));
        adminSettings.setCreatedTime(new Date().getTime());
      }
      adminSettings.setKey("web-enduser");
      JsonNode dataJson = mapper.convertValue(serverUrlDto, JsonNode.class);
      adminSettings.setJsonValue(dataJson);
      return new ResponseEntity<>(checkNotNull(adminSettingsService
          .saveAdminSettings(securityUser.getTenantId(), adminSettings)), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PermitAll
  @GetMapping("web-enduser")
  @ResponseBody
  @ApiOperation(value = "get url server chạy web end user")
  public ResponseEntity<?> getUrlWebEndUser() throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      AdminSettings adminSettings = adminSettingsService.findAdminSettingsByKey(securityUser.getTenantId(), "web-enduser");
      return new ResponseEntity<>(checkNotNull(adminSettings.getJsonValue()), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PermitAll
  @PostMapping("web-admin")
  @ResponseBody
  @ApiOperation(value = "setting server web admin")
  public ResponseEntity<?> createOrUpdateUrlWebAdmin(@RequestBody ServerUrlDto serverUrlDto) throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      AdminSettings adminSettings = adminSettingsService.findAdminSettingsByKey(securityUser.getTenantId(), "web-admin");
      if (adminSettings == null) {
        adminSettings = new AdminSettings();
        adminSettings.setId(new AdminSettingsId(UUID.randomUUID()));
        adminSettings.setCreatedTime(new Date().getTime());
      }
      adminSettings.setKey("web-admin");
      JsonNode dataJson = mapper.convertValue(serverUrlDto, JsonNode.class);
      adminSettings.setJsonValue(dataJson);
      return new ResponseEntity<>(checkNotNull(adminSettingsService
          .saveAdminSettings(securityUser.getTenantId(), adminSettings)), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PermitAll
  @GetMapping("web-admin")
  @ResponseBody
  @ApiOperation(value = "get url server admin")
  public ResponseEntity<?> getUrlWebAdmin() throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      AdminSettings adminSettings = adminSettingsService.findAdminSettingsByKey(securityUser.getTenantId(), "web-admin");
      return new ResponseEntity<>(checkNotNull(adminSettings.getJsonValue()), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PermitAll
  @PostMapping("video-storage")
  @ResponseBody
  @ApiOperation(value = "setting server video storage")
  public ResponseEntity<?> createOrUpdateUrlVideoStorage(@RequestBody ServerUrlDto serverUrlDto) throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      AdminSettings adminSettings = adminSettingsService.findAdminSettingsByKey(securityUser.getTenantId(), "video-storage");
      if (adminSettings == null) {
        adminSettings = new AdminSettings();
        adminSettings.setId(new AdminSettingsId(UUID.randomUUID()));
        adminSettings.setCreatedTime(new Date().getTime());
      }
      adminSettings.setKey("video-storage");
      JsonNode dataJson = mapper.convertValue(serverUrlDto, JsonNode.class);
      adminSettings.setJsonValue(dataJson);
      return new ResponseEntity<>(checkNotNull(adminSettingsService
          .saveAdminSettings(securityUser.getTenantId(), adminSettings)), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PermitAll
  @GetMapping("video-storage")
  @ResponseBody
  @ApiOperation(value = "get url server VideoStorage")
  public ResponseEntity<?> getUrlVideoStorage() throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      AdminSettings adminSettings = adminSettingsService.findAdminSettingsByKey(securityUser.getTenantId(), "video-storage");
      return new ResponseEntity<>(checkNotNull(adminSettings.getJsonValue()), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

}
