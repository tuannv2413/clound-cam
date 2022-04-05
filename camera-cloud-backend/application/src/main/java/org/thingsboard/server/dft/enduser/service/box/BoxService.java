package org.thingsboard.server.dft.enduser.service.box;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dto.box.BoxDetailDto;
import org.thingsboard.server.dft.enduser.dto.box.BoxEditDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraScanResult;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface BoxService {
  BoxEditDto create(BoxEditDto boxEditDto, UUID tbDeviceId, SecurityUser securityUser) throws JSONException;

  BoxEditDto update(BoxEditDto boxEditDto, SecurityUser securityUser);

  DeviceId deleteById(UUID id, UUID teantId);

  BoxDetailDto getBoxDetailById(UUID id, SecurityUser securityUser) throws ExecutionException, InterruptedException, JSONException;

  BoxDetailDto getBoxDetailByTenantIdAndTbDeviceId(UUID tenantId, UUID tbDeviceId);

  PageData<BoxDetailDto> findAllBySearchText(Pageable pageable, Boolean active,
                                             String searchText, SecurityUser securityUser, boolean fullOption)
          throws ExecutionException, InterruptedException, JSONException;

  boolean checkExistSerialNumber(String serialNumber);

  List<BoxDetailDto> getAllBoxDetailByTenantId(SecurityUser securityUser, Sort sort) throws ExecutionException, InterruptedException, JSONException;

  List<CameraScanResult> getListDeviceCameraInBox(UUID boxId, SecurityUser securityUser) throws ExecutionException, InterruptedException, JSONException, URISyntaxException;

  boolean checkExistByTenantIdAndBoxName(UUID tenantId, String boxName);

  boolean checkExistByTenantIdAndBoxNameAndIdNot(UUID tenantId, String boxName, UUID id);

  void saveDeviceNameToListDeviceName(UUID boxId, UUID tbDeviceId, SecurityUser securityUser)
      throws ExecutionException, InterruptedException, JSONException;

  void removeDeviceNameFromListDeviceName(UUID boxId, UUID tbDeviceId, SecurityUser securityUser)
      throws ExecutionException, InterruptedException, JSONException, JsonProcessingException;
}
