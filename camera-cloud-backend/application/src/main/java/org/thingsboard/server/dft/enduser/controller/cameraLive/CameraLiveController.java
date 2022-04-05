package org.thingsboard.server.dft.enduser.controller.cameraLive;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.UUID;

@Slf4j
@RestController
@TbCoreComponent
@RequestMapping("/api/camera-live")
public class CameraLiveController extends BaseController {

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
  @PostMapping(value = "/ping/{cameraId}")
  @ResponseBody
  @ApiOperation(value = "Test ping để bật live stream tới box",
      notes = "truyền vào id camera muốn bật live")
  public ResponseEntity<?> connectToStream(@PathVariable("cameraId") UUID cameraId) throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      cameraLiveService.setCameraOnline(cameraId, securityUser);
      JSONObject pong = new JSONObject();
      pong.put("data", "pong");
      return new ResponseEntity<>(pong.toString(), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
  @GetMapping
  @ResponseBody
  @ApiOperation(value = "Lấy list camera theo group",
      notes = "Truyền null sẽ lấy ra camera chưa có group nào")
  public ResponseEntity<?> getListCameraByGroupId(@RequestParam(value = "groupId", required = false) UUID groupId,
                                                  @RequestParam(value = "searchText", defaultValue = "", required = false) String searchText)
      throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      searchText = searchText.trim();
      return new ResponseEntity<>(cameraService.getCameraStreamByIdGroup(groupId, searchText, securityUser), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
  @GetMapping("/all")
  @ResponseBody
  @ApiOperation(value = "Lấy toàn bộ camera theo group camera")
  public ResponseEntity<?> getAllCameraInGroup(@RequestParam(value = "searchText", defaultValue = "", required = false) String searchText) throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      searchText = searchText.trim();
      return new ResponseEntity<>(cameraService.getListCameraGroup(searchText, securityUser), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

}
