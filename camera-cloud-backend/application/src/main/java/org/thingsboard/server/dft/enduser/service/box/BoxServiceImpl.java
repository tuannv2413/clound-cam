package org.thingsboard.server.dft.enduser.service.box;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.util.concurrent.FutureCallback;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.kv.BaseAttributeKvEntry;
import org.thingsboard.server.common.data.kv.JsonDataEntry;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dao.attributes.AttributesDao;
import org.thingsboard.server.dao.device.DeviceDao;
import org.thingsboard.server.dao.timeseries.TimeseriesLatestDao;
import org.thingsboard.server.dft.enduser.dao.box.BoxDao;
import org.thingsboard.server.dft.enduser.dao.camera.CameraDao;
import org.thingsboard.server.dft.enduser.dto.box.BoxDetailDto;
import org.thingsboard.server.dft.enduser.dto.box.BoxEditDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraDetailDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraScanResult;
import org.thingsboard.server.dft.util.constant.BoxInfoKeyConstant;
import org.thingsboard.server.dft.util.constant.CameraInfoKeyConstant;
import org.thingsboard.server.service.security.model.SecurityUser;
import org.thingsboard.server.service.telemetry.TelemetrySubscriptionService;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class BoxServiceImpl implements BoxService {

  private final BoxDao boxDao;
  private final AttributesDao attributesDao;
  private final TimeseriesLatestDao timeseriesLatestDao;
  private final DeviceDao deviceDao;
  private final CameraDao cameraDao;

  @Autowired
  protected TelemetrySubscriptionService tsSubService;

  @Autowired
  public BoxServiceImpl(
      BoxDao boxDao,
      AttributesDao attributesDao,
      TimeseriesLatestDao timeseriesLatestDao,
      DeviceDao deviceDao, CameraDao cameraDao) {
    this.boxDao = boxDao;
    this.attributesDao = attributesDao;
    this.timeseriesLatestDao = timeseriesLatestDao;
    this.deviceDao = deviceDao;
    this.cameraDao = cameraDao;
  }

  @Override
  public BoxEditDto create(BoxEditDto boxEditDto, UUID tbDeviceId, SecurityUser securityUser)
      throws JSONException {
    return boxDao.create(boxEditDto, tbDeviceId, securityUser);
  }

  @Override
  public BoxEditDto update(BoxEditDto boxEditDto, SecurityUser securityUser) {
    return boxDao.update(boxEditDto, securityUser);
  }

  @Override
  public DeviceId deleteById(UUID id, UUID tenantId) {
    return boxDao.deleteById(id, tenantId);
  }

  @Override
  public BoxDetailDto getBoxDetailById(UUID id, SecurityUser securityUser)
      throws ExecutionException, InterruptedException, JSONException {
    BoxDetailDto boxDetailDto = boxDao.getBoxDetailById(id);
    if (boxDetailDto != null) {
      boxDetailDto = addMoreInfo(boxDetailDto, securityUser);
    }

    return boxDetailDto;
  }

  @Override
  public BoxDetailDto getBoxDetailByTenantIdAndTbDeviceId(UUID tenantId, UUID tbDeviceId) {
    return boxDao.getBoxDetailByTenantIdAndTbDeviceId(tenantId, tbDeviceId);
  }

  @Override
  public PageData<BoxDetailDto> findAllBySearchText(
      Pageable pageable, Boolean active, String searchText, SecurityUser securityUser, boolean fullOption)
      throws ExecutionException, InterruptedException, JSONException {
    PageData<BoxDetailDto> boxDetailDtoPageData =
        boxDao.findAllBySearchText(pageable, active, searchText,
                securityUser, fullOption);
    for (BoxDetailDto boxDetailDto : boxDetailDtoPageData.getData()) {
      addMoreInfo(boxDetailDto, securityUser);
    }
    return boxDetailDtoPageData;
  }

  @Override
  public boolean checkExistSerialNumber(String serialNumber) {
    return boxDao.checkExistSerialNumber(serialNumber);
  }

  @Override
  public List<BoxDetailDto> getAllBoxDetailByTenantId(SecurityUser securityUser, Sort sort)
      throws ExecutionException, InterruptedException, JSONException {
    List<BoxDetailDto> boxDetailDtos = boxDao.findAll(securityUser.getTenantId().getId(), sort);
    for (BoxDetailDto boxDetailDto : boxDetailDtos) {
      addMoreInfo(boxDetailDto, securityUser);
    }
    return boxDetailDtos;
  }

  @Transactional
  @Override
  public List<CameraScanResult> getListDeviceCameraInBox(UUID boxId, SecurityUser securityUser)
          throws ExecutionException, InterruptedException, JSONException, URISyntaxException {
    BoxDetailDto boxDetailDto = getBoxDetailById(boxId, securityUser);

    List<CameraScanResult> cameraScanResults = new ArrayList<>();
    AttributeKvEntry listCameraAttribute =
        findAttribute(
            securityUser.getTenantId(),
            new DeviceId(boxDetailDto.getTbDeviceId()),
            BoxInfoKeyConstant.LISTCAMERA,
            DataConstants.CLIENT_SCOPE);
    if (listCameraAttribute != null) {
      JSONArray listCameraJson = new JSONArray(listCameraAttribute.getValueAsString());
      for (int i = 0; i < listCameraJson.length(); i++) {
        CameraScanResult cameraScanResult = new CameraScanResult();
        JSONObject cameraJson = listCameraJson.getJSONObject(i);
        cameraScanResult.setBoxName(boxDetailDto.getBoxName());
        cameraScanResult.setBoxId(boxDetailDto.getId());
        cameraScanResult.setDeviceName(
            cameraJson.isNull("name") ? "" : cameraJson.getString("name"));
        cameraScanResult.setIpv4(cameraJson.isNull("ipv4") ? "" : cameraJson.getString("ipv4").split(":")[0]);
        cameraScanResult.setOnvifUrl(cameraJson.isNull("onvifUrl") ? "" : cameraJson.getString("onvifUrl"));
        cameraScanResult.setOnvifPort(exactOnvifPort(cameraScanResult.getOnvifUrl()));
        cameraScanResult.setProtocol(cameraJson.isNull("ipv4") ? "" : "RTSP");
        CameraDetailDto cameraDetailDto = checkCameraHasAuth(boxId, cameraScanResult.getIpv4());
        cameraScanResult.setHasAuth(cameraDetailDto != null);
        if (cameraScanResult.isHasAuth()) {
          AttributeKvEntry resolutionAttribute =
              findAttribute(new TenantId(cameraDetailDto.getTenantId()), new DeviceId(cameraDetailDto.getTbDeviceId()),
                  CameraInfoKeyConstant.RESOLUTION, DataConstants.CLIENT_SCOPE);
          if (resolutionAttribute != null) {
            cameraScanResult.setResolution(resolutionAttribute.getValueAsString());
          }
        }
        cameraScanResults.add(cameraScanResult);
      }
    }
    return cameraScanResults;
  }

  @Override
  public boolean checkExistByTenantIdAndBoxName(UUID tenantId, String boxName) {
    return boxDao.existsByBoxNameAndTenantId(boxName, tenantId);
  }

  @Override
  public boolean checkExistByTenantIdAndBoxNameAndIdNot(UUID tenantId, String boxName, UUID id) {
    return boxDao.existsByBoxNameAndTenantIdAndIdNot(boxName, tenantId, id);
  }

  @Override
  public void saveDeviceNameToListDeviceName(UUID boxId, UUID tbDeviceId, SecurityUser securityUser)
      throws ExecutionException, InterruptedException, JSONException {
    BoxDetailDto boxDetailDto = getBoxDetailById(boxId, securityUser);
    AttributeKvEntry listDeviceNameAttribute =
        findAttribute(
            securityUser.getTenantId(),
            new DeviceId(boxDetailDto.getTbDeviceId()),
            BoxInfoKeyConstant.LISTDEVICENAME,
            DataConstants.SHARED_SCOPE);
    List<String> listDeviceName = new ArrayList<>();
    if (listDeviceNameAttribute != null) {
      JSONObject jsonObject = new JSONObject(listDeviceNameAttribute.getValueAsString());
      JSONArray jsonArray = jsonObject.getJSONArray(BoxInfoKeyConstant.LISTDEVICENAME);
      for (int i = 0; i < jsonArray.length(); i++) {
        listDeviceName.add(jsonArray.getString(i));
      }
    }
    Device device = deviceDao.findDeviceInfoById(securityUser.getTenantId(), tbDeviceId);
    listDeviceName.add(device.getName());
    JSONObject listDeviceNameJson = new JSONObject();
    listDeviceNameJson.put(BoxInfoKeyConstant.LISTDEVICENAME, new JSONArray(listDeviceName));
    listDeviceNameAttribute =
        new BaseAttributeKvEntry(
            System.currentTimeMillis(),
            new JsonDataEntry(BoxInfoKeyConstant.LISTDEVICENAME, listDeviceNameJson.toString()));
    //    attributesDao.save(securityUser.getTenantId(), new DeviceId(boxDetailDto.getTbDeviceId()),
    // DataConstants.SHARED_SCOPE,
    //        listDeviceNameAttribute);
    tsSubService.saveAndNotify(
        securityUser.getTenantId(),
        new DeviceId(boxDetailDto.getTbDeviceId()),
        DataConstants.SHARED_SCOPE,
        List.of(listDeviceNameAttribute),
        new FutureCallback<>() {
          @Override
          public void onSuccess(@Nullable Void tmp) {
            log.info("Save list deivce name success!!");
          }

          @Override
          public void onFailure(Throwable t) {
            log.info("Save list deivce name failde!!");
          }
        });
  }

  @Override
  public void removeDeviceNameFromListDeviceName(
      UUID boxId, UUID tbDeviceId, SecurityUser securityUser)
      throws ExecutionException, InterruptedException, JSONException, JsonProcessingException {
    BoxDetailDto boxDetailDto = getBoxDetailById(boxId, securityUser);
    AttributeKvEntry listDeviceNameAttribute =
        findAttribute(
            securityUser.getTenantId(),
            new DeviceId(boxDetailDto.getTbDeviceId()),
            BoxInfoKeyConstant.LISTDEVICENAME,
            DataConstants.SHARED_SCOPE);
    List<String> listDeviceName = new ArrayList<>();
    if (listDeviceNameAttribute != null) {
      JSONObject jsonObject = new JSONObject(listDeviceNameAttribute.getValueAsString());
      JSONArray jsonArray = jsonObject.getJSONArray(BoxInfoKeyConstant.LISTDEVICENAME);
      for (int i = 0; i < jsonArray.length(); i++) {
        listDeviceName.add(jsonArray.getString(i));
      }
    }
    Device device = deviceDao.findDeviceInfoById(securityUser.getTenantId(), tbDeviceId);
    if (device != null) {
      listDeviceName.remove(device.getName());
    }
    JSONObject listDeviceNameJson = new JSONObject();
    listDeviceNameJson.put(BoxInfoKeyConstant.LISTDEVICENAME, new JSONArray(listDeviceName));
    listDeviceNameAttribute =
        new BaseAttributeKvEntry(
            System.currentTimeMillis(),
            new JsonDataEntry(BoxInfoKeyConstant.LISTDEVICENAME, listDeviceNameJson.toString()));
    //    attributesDao.save(securityUser.getTenantId(), new DeviceId(boxDetailDto.getTbDeviceId()),
    // DataConstants.SHARED_SCOPE,
    //        listDeviceNameAttribute);
    tsSubService.saveAndNotify(
        securityUser.getTenantId(),
        new DeviceId(boxDetailDto.getTbDeviceId()),
        DataConstants.SHARED_SCOPE,
        List.of(listDeviceNameAttribute),
        new FutureCallback<>() {
          @Override
          public void onSuccess(@Nullable Void tmp) {
            log.info("Save list deivce name success!!");
          }

          @Override
          public void onFailure(Throwable t) {
            log.info("Save list deivce name failde!!");
          }
        });
  }

  private AttributeKvEntry findAttribute(
      TenantId tenantId, DeviceId deviceId, String key, String scope)
      throws ExecutionException, InterruptedException {
    Optional<AttributeKvEntry> optionalAttributeKvEntry =
        attributesDao.find(tenantId, deviceId, scope, key).get();
    if (optionalAttributeKvEntry.isEmpty()) {
      return null;
    }
    return optionalAttributeKvEntry.orElse(null);
  }

  private TsKvEntry findLatestTelemetry(TenantId tenantId, DeviceId deviceId, String key)
      throws ExecutionException, InterruptedException {
    return timeseriesLatestDao.findLatest(tenantId, deviceId, key).get();
  }

  private BoxDetailDto addMoreInfo(BoxDetailDto boxDetailDto, SecurityUser securityUser)
      throws ExecutionException, InterruptedException, JSONException {
    if (boxDetailDto != null) {
      DeviceId deviceId = new DeviceId(boxDetailDto.getTbDeviceId());
      // lấy thông số attribute từ box vậy lý
      String ipv4;
      AttributeKvEntry ipv4Attribute =
          findAttribute(
              securityUser.getTenantId(),
              deviceId,
              BoxInfoKeyConstant.IPV4,
              DataConstants.CLIENT_SCOPE);
      if (ipv4Attribute != null) {
        ipv4 = ipv4Attribute.getValueAsString();
        boxDetailDto.setIpv4(ipv4);
      }

      String firmware;
      AttributeKvEntry firmwareAttribute =
          findAttribute(
              securityUser.getTenantId(),
              deviceId,
              BoxInfoKeyConstant.FIRMWARE,
              DataConstants.CLIENT_SCOPE);
      if (firmwareAttribute != null) {
        firmware = firmwareAttribute.getValueAsString();
        boxDetailDto.setFirmware(firmware);
      }

      // check list name để biết được những camera nào đã add vào box
      AttributeKvEntry listDeviceNameAttribute =
          findAttribute(
              securityUser.getTenantId(),
              new DeviceId(boxDetailDto.getTbDeviceId()),
              BoxInfoKeyConstant.LISTDEVICENAME,
              DataConstants.SHARED_SCOPE);
      List<String> listDeviceName = new ArrayList<>();
      if (listDeviceNameAttribute != null) {
        JSONObject jsonObject = new JSONObject(listDeviceNameAttribute.getValueAsString());
        JSONArray jsonArray = jsonObject.getJSONArray(BoxInfoKeyConstant.LISTDEVICENAME);
        for (int i = 0; i < jsonArray.length(); i++) {
          listDeviceName.add(jsonArray.getString(i));
        }
      }
      boxDetailDto.setTotalCamera(listDeviceName.size());

      String model;
      AttributeKvEntry modelAttribute =
          findAttribute(
              securityUser.getTenantId(),
              deviceId,
              BoxInfoKeyConstant.MODEL,
              DataConstants.CLIENT_SCOPE);
      if (modelAttribute != null) {
        model = modelAttribute.getValueAsString();
        boxDetailDto.setModel(model);
      }

      // lấy thông số telemetry từ box vậy lý
      String cpu =
          findLatestTelemetry(securityUser.getTenantId(), deviceId, BoxInfoKeyConstant.CPU)
              .getValueAsString();
      String ram =
          findLatestTelemetry(securityUser.getTenantId(), deviceId, BoxInfoKeyConstant.RAM)
              .getValueAsString();
      String temperature =
          findLatestTelemetry(securityUser.getTenantId(), deviceId, BoxInfoKeyConstant.TEMPERATURE)
              .getValueAsString();
      if (cpu != null) {
        boxDetailDto.setCpu(Double.parseDouble(cpu));
      }
      if (ram != null) {
        boxDetailDto.setRam(Double.parseDouble(ram));
      }
      if (cpu != null) {
        boxDetailDto.setTemperature(Double.parseDouble(temperature));
      }

      // lấy trạng thái kết nối từ db thingsboard
      AttributeKvEntry activeAttribute =
          findAttribute(
              securityUser.getTenantId(),
              deviceId,
              BoxInfoKeyConstant.ACTIVE,
              DataConstants.SERVER_SCOPE);
      if (activeAttribute != null) {
        boxDetailDto.setActive(Boolean.parseBoolean(activeAttribute.getValueAsString()));
      }
      AttributeKvEntry lastActivityTimeAttriute =
          findAttribute(
              securityUser.getTenantId(),
              deviceId,
              BoxInfoKeyConstant.LAST_ACTIVITY_TIME,
              DataConstants.SERVER_SCOPE);
      if (lastActivityTimeAttriute != null) {
        boxDetailDto.setLastActivityTime(
            Long.parseLong(lastActivityTimeAttriute.getValueAsString()));
      }
      AttributeKvEntry lastConnectTimeAttriute =
          findAttribute(
              securityUser.getTenantId(),
              deviceId,
              BoxInfoKeyConstant.LAST_CONNECT_TIME,
              DataConstants.SERVER_SCOPE);
      if (lastConnectTimeAttriute != null) {
        boxDetailDto.setLastConnectTime(Long.parseLong(lastConnectTimeAttriute.getValueAsString()));
      }
    }
    return boxDetailDto;
  }

  private CameraDetailDto checkCameraHasAuth(UUID boxId, String ipv4) {
    return cameraDao.existByBoxIdAndIpv4(boxId, ipv4);
  }

  private int exactOnvifPort(String onvifUrp) throws URISyntaxException {
    URI uri = new URI(onvifUrp);
    if (uri.getPort() == -1) {
      return 80;
    }
    return uri.getPort();
  }

  private String exactOnvifIp(String onvifUrp) throws URISyntaxException {
    URI uri = new URI(onvifUrp);
    return uri.getHost();
  }
}
