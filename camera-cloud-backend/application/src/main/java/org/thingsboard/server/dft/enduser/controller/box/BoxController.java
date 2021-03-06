package org.thingsboard.server.dft.enduser.controller.box;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.thingsboard.rule.engine.api.msg.DeviceNameOrTypeUpdateMsg;
import org.thingsboard.server.common.data.*;
import org.thingsboard.server.common.data.device.profile.DeviceProfileData;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.DeviceProfileId;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.common.data.relation.EntityRelation;
import org.thingsboard.server.common.data.relation.RelationTypeGroup;
import org.thingsboard.server.dft.enduser.controller.AbstractSendRpcController;
import org.thingsboard.server.dft.enduser.dto.box.BoxDetailDto;
import org.thingsboard.server.dft.enduser.dto.box.BoxEditDto;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;
import org.thingsboard.server.service.security.permission.Operation;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@TbCoreComponent
@RequestMapping("/api/box")
public class BoxController extends AbstractSendRpcController {

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @GetMapping
  @ResponseBody
  @ApiOperation(value = "L???y danh s??ch box c?? filter",
      notes = "C?? th??? sort theo m???t s??? tr?????ng sau createdTime, boxName"
  )
  public ResponseEntity<?> getAllByTextSearch(
      @RequestParam(name = "pageSize") int pageSize,
      @RequestParam(name = "page") int page,
      @RequestParam(required = false, defaultValue = "createdTime") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortOrder,
      @RequestParam(required = false) Boolean active,
      @RequestParam(required = false, defaultValue = "") String textSearch,
      @RequestParam(required = false, defaultValue = "true") boolean fullOption) throws ThingsboardException {
    try {
      textSearch = textSearch.trim();
      SecurityUser securityUser = getCurrentUser();
      Pageable pageable =
          PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty);
      return new ResponseEntity<>(checkNotNull(boxService.findAllBySearchText(pageable, active,
          textSearch, securityUser, fullOption)), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @GetMapping("/no-page")
  @ResponseBody
  @ApiOperation(value = "L???y danh s??ch box to??n b???")
  public ResponseEntity<?> getAllBox() throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      Sort sort = Sort.by("createdTime").ascending();
      return new ResponseEntity<>(checkNotNull(boxService.getAllBoxDetailByTenantId(securityUser, sort)), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @GetMapping("{id}")
  @ResponseBody
  @ApiOperation(value = "L???y th??ng tin chi ti???t box theo id")
  public ResponseEntity<?> getBoxDetailById(@PathVariable("id") UUID id) throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      return new ResponseEntity<>(checkNotNull(boxService.getBoxDetailById(id, securityUser)), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  //  @Transactional(rollbackFor = {Exception.class})
  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @PostMapping
  @ResponseBody
  @ApiOperation(value = "Th??m m???i box camera",
      notes = "Kh??ng c???n nh???p id c???a box")
  public ResponseEntity<?> saveNewBox(@RequestBody @Valid BoxEditDto boxEditDto) throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
      boxEditDto.setBoxName(boxEditDto.getBoxName().trim());
      if (!mediaBoxService.checkExistSerialNumber(boxEditDto.getSerialNumber())) {
        throw new ThingsboardException("Serial Number kh??ng t???n t???i trong h??? th???ng qu???n l??!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }

      if (boxService.checkExistByTenantIdAndBoxName(securityUser.getTenantId().getId(), boxEditDto.getBoxName())) {
        throw new ThingsboardException("T??n box ???? t???n t???i!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }

      if (boxService.checkExistSerialNumber(boxEditDto.getSerialNumber())) {
        throw new ThingsboardException("S??? Serial Number ???? t???n t???i!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }

      boxEditDto.setBoxName(boxEditDto.getBoxName().trim());

      // Th??m m???i device IoT trong thingsboard ???ng v???i box
      Device device = new Device();
      device.setName(boxEditDto.getSerialNumber());
      device.setLabel(boxEditDto.getBoxName());
      JsonNode addtionalInfo =
          mapper.readTree(
              "{\"gateway\":true,\"overwriteActivityTime\":false,\"description\":\"\"}");
      device.setAdditionalInfo(addtionalInfo);

      // Create deviceProfile cho box phuc v??? ph???n c???nh b??o
      DeviceProfile deviceProfile = new DeviceProfile();
      deviceProfile.setTenantId(getCurrentUser().getTenantId());
      deviceProfile.setName(boxEditDto.getSerialNumber() + "_DEVICEPROFILE");

      DeviceProfileData deviceProfileData = mapper.readValue("{\"configuration\":{\"type\":\"DEFAULT\"},\"transportConfiguration\":{\"type\":\"DEFAULT\"},\"alarms\":null,\"provisionConfiguration\":{\"type\":\"DISABLED\"}}", DeviceProfileData.class);
      deviceProfile.setProfileData(deviceProfileData);
      deviceProfile.setType(DeviceProfileType.DEFAULT);
      deviceProfile.setProvisionType(DeviceProfileProvisionType.DISABLED);
      deviceProfile.setTransportType(DeviceTransportType.DEFAULT);
      DeviceProfile savedDeviceProfile = checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfile));
      tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
      tbClusterService.broadcastEntityStateChangeEvent(deviceProfile.getTenantId(), savedDeviceProfile.getId(), ComponentLifecycleEvent.CREATED);
      device.setDeviceProfileId(savedDeviceProfile.getId());

      //L??u l???i box v?? n???p xu???ng queue in memory
      device.setTenantId(securityUser.getTenantId());
      Device savedDevice =
          deviceService.saveDeviceWithAccessToken(device, boxEditDto.getSerialNumber());
      tbClusterService.onDeviceUpdated(savedDevice, null);
      tbClusterService.pushMsgToCore(
          new DeviceNameOrTypeUpdateMsg(
              savedDevice.getTenantId(),
              savedDevice.getId(),
              savedDevice.getName(),
              savedDevice.getType()),
          null);
      tbClusterService.broadcastEntityStateChangeEvent(
          savedDevice.getTenantId(), savedDevice.getId(), ComponentLifecycleEvent.CREATED);

      return new ResponseEntity<>(
          checkNotNull(boxService.create(boxEditDto, savedDevice.getUuidId(), securityUser)), HttpStatus.CREATED);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  //  @Transactional(rollbackFor = {Exception.class})
  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @PutMapping("{id}")
  @ResponseBody
  @ApiOperation(value = "C???p nh???t th??ng tin by box")
  public ResponseEntity<?> update(@RequestBody @Valid BoxEditDto boxEditDto, @PathVariable("id") UUID id) throws ThingsboardException {
    try {
      boxEditDto.setBoxName(boxEditDto.getBoxName().trim());
      SecurityUser securityUser = getCurrentUser();

      if (boxService.checkExistByTenantIdAndBoxNameAndIdNot(securityUser.getTenantId().getId(), boxEditDto.getBoxName(), id)) {
        throw new ThingsboardException("T??n ???? t???n t??i ???? t???n t???i!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
      }

      boxEditDto.setId(id);
      return new ResponseEntity<>(checkNotNull(boxService.update(boxEditDto, securityUser)), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @DeleteMapping("{id}")
  @ResponseBody
  @ApiOperation(value = "X??a box camera by id (UUID)"
  )
  public ResponseEntity<?> deleteById(@PathVariable("id") UUID id) throws ThingsboardException {
    try {
      SecurityUser securityUser = getCurrentUser();
//      cameraRecordSettingService.deleteByBoxId(id, securityUser.getTenantId().getId());
//      cameraService.deleteByBoxId(securityUser.getTenantId().getId(), id);
      DeviceId deviceBoxId = boxService.deleteById(id, securityUser.getTenantId().getId());
      checkNotNull(deviceBoxId);

      RelationTypeGroup typeGroup = RelationTypeGroup.COMMON;
      List<EntityRelation> listDeviceCamId = relationService.findByFrom(securityUser.getTenantId(), deviceBoxId, typeGroup);

      // L???y to??n b??? danh s??ch camera device thu???c box ????? x??a
      for (EntityRelation entityRelation : listDeviceCamId) {
        DeviceId deviceCameraId = new DeviceId(entityRelation.getTo().getId());
        Device device = checkDeviceId(deviceCameraId, Operation.DELETE);
        deviceService.deleteDevice(getCurrentUser().getTenantId(), deviceCameraId);
        tbClusterService.onDeviceDeleted(device, null);

        DeviceProfileId deviceProfileId = device.getDeviceProfileId();
        DeviceProfile deviceProfile = checkDeviceProfileId(deviceProfileId, Operation.DELETE);
        deviceProfileService.deleteDeviceProfile(getTenantId(), deviceProfileId);

        tbClusterService.onDeviceProfileDelete(deviceProfile, null);
        tbClusterService.broadcastEntityStateChangeEvent(deviceProfile.getTenantId(), deviceProfile.getId(), ComponentLifecycleEvent.DELETED);
      }

      // G???i th??ng b??o x??a box cho b??x
      JSONObject requestParam = new JSONObject();
      requestParam.put("value", "DELETE_BOX");
      JSONObject requestBody = new JSONObject();
      requestBody.put("method", "SYSTEM");
      requestBody.put("params", requestParam);
      log.info(requestBody.toString());
      handleDeviceRPCRequestNoResponse(deviceBoxId, requestBody.toString(), securityUser);

      // X??a device tr??n thingsboard
      Device device = checkDeviceId(deviceBoxId, Operation.DELETE);
      deviceService.deleteDevice(getCurrentUser().getTenantId(), deviceBoxId);
      tbClusterService.onDeviceDeleted(device, null);

      DeviceProfileId deviceProfileId = device.getDeviceProfileId();
      DeviceProfile deviceProfile = checkDeviceProfileId(deviceProfileId, Operation.DELETE);
      deviceProfileService.deleteDeviceProfile(getTenantId(), deviceProfileId);

      tbClusterService.onDeviceProfileDelete(deviceProfile, null);
      tbClusterService.broadcastEntityStateChangeEvent(deviceProfile.getTenantId(), deviceProfile.getId(), ComponentLifecycleEvent.DELETED);


      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @ApiOperation(value = "G???i c??c l???nh ??i???u khi???n xu???ng box",
      notes = "Gi?? tr??? ??i???u khi???n: UPDATE - update firmware, RESTART - kh???i ?????ng l???i, SCAN - scan l???i danh s??ch camera")
  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN')")
  @RequestMapping(value = "/send-rpc/{id}/{valueControl}", method = RequestMethod.POST)
  @ResponseBody
  public DeferredResult<ResponseEntity> handleOneWayDeviceRPCRequest(
      @PathVariable("id") UUID id,
      @PathVariable("valueControl") String valueControl) throws ThingsboardException, JSONException {
    try {
      SecurityUser securityUser = new SecurityUser();
      BoxDetailDto boxDetailDto = boxService.getBoxDetailById(id, securityUser);
      checkNotNull(boxDetailDto);
      JSONObject requestParam = new JSONObject();
      requestParam.put("value", valueControl);

      JSONObject requestBody = new JSONObject();
      requestBody.put("method", "SYSTEM");
      requestBody.put("params", requestParam);
      log.info(requestBody.toString());
      return handleDeviceRPCRequest(true, new DeviceId(boxDetailDto.getTbDeviceId()),
          requestBody.toString(), HttpStatus.REQUEST_TIMEOUT, HttpStatus.CONFLICT);
    } catch (Exception e) {
      throw handleException(e);
    }

  }


}
