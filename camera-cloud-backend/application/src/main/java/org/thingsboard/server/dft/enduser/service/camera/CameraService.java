package org.thingsboard.server.dft.enduser.service.camera;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dto.camera.AddCameraGroupDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDetailDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraEditDto;
import org.thingsboard.server.dft.enduser.dto.cameraGroup.CameraStreamGroup;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface CameraService {

  PageData<CameraDto> getPageCameraBySearch(Pageable pageable, String textSearch, UUID boxId, SecurityUser securityUser) throws ExecutionException, InterruptedException, ThingsboardException, JSONException, JsonProcessingException;

  CameraStreamGroup getCameraStreamByIdGroup(UUID groupId, String searchText, SecurityUser securityUser) throws JSONException, ExecutionException, InterruptedException, JsonProcessingException, ThingsboardException;

  List<CameraStreamGroup> getListCameraGroup(String searchText, SecurityUser securityUser) throws JSONException, ExecutionException, InterruptedException, JsonProcessingException, ThingsboardException;

  CameraEditDto save(CameraEditDto cameraEditDto, SecurityUser securityUser);

  CameraEditDto update(CameraEditDto cameraEditDto, SecurityUser securityUser);

  CameraEditDto changeBox(CameraEditDto cameraEditDto, SecurityUser securityUser);

  CameraEditDto getCameraEditDtoById(UUID id);

  CameraEditDto getCameraEditDtoByTenantIdAndTbDeviceId(UUID tenantId, UUID tbDeviceId);

  CameraDetailDto getCameraDetailDtoById(UUID id) throws JSONException, ExecutionException, InterruptedException, JsonProcessingException;

  void deleteById(UUID tenantId, UUID id);

  void deleteByBoxId(UUID tenantId, UUID boxId);

  boolean checkCameraIdInDevice(TenantId tenantId, DeviceId deviceId, UUID cameraId) throws ExecutionException, InterruptedException;

  boolean existsByTbDeviceIdAndTenantId(UUID tbDeviceId, UUID tenantId);

  boolean existsByTbDeviceIdAndTenantIdAndBoxId(UUID tbDeviceId, UUID tenantId, UUID boxId);

  boolean existsByTbDeviceIdAndTenantIdAndIdNot(UUID tbDeviceId, UUID tenantId, UUID id);

  boolean existsByCameraNameAndTenantId(String cameraName, UUID tenantId);

  boolean existsByCameraNameAndTenantIdAndIdNot(String cameraName, UUID tenantId, UUID id);

  void deleteFromGroup(UUID tenantId, UUID id);

  void addToGroup(UUID tenantId, UUID id1, AddCameraGroupDto addCameraGroupDto);
}
