package org.thingsboard.server.dft.enduser.service.camera;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.DataConstants;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.kv.TsKvEntry;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dao.attributes.AttributesDao;
import org.thingsboard.server.dao.timeseries.TimeseriesLatestDao;
import org.thingsboard.server.dft.enduser.dao.camera.CameraDao;
import org.thingsboard.server.dft.enduser.dao.user.EndUserDao;
import org.thingsboard.server.dft.enduser.dto.camera.*;
import org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.CameraProfileSetting;
import org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.setting.ConverResolutionSupport;
import org.thingsboard.server.dft.enduser.dto.camera.settingAttribute.setting.ResolutionSupport;
import org.thingsboard.server.dft.enduser.dto.cameraGroup.CameraStreamGroup;
import org.thingsboard.server.dft.enduser.dto.user.EndUserDto;
import org.thingsboard.server.dft.enduser.service.cameraLive.CameraLiveService;
import org.thingsboard.server.dft.mbgadmin.dao.clTenant.CLTenantDao;
import org.thingsboard.server.dft.util.constant.CameraInfoKeyConstant;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class CameraServiceImpl implements CameraService {

  private final ObjectMapper mapper = new ObjectMapper();

  private final CameraDao cameraDao;
  private final AttributesDao attributesDao;
  private final TimeseriesLatestDao timeseriesLatestDao;
  private final CameraLiveService cameraLiveService;
  private final CustomerCameraPermissionService customerCameraPermissionService;
  private final EndUserDao endUserDao;
  private final CLTenantDao clTenantDao;

  @Autowired
  public CameraServiceImpl(CameraDao cameraDao, AttributesDao attributesDao,
                           TimeseriesLatestDao timeseriesLatestDao,
                           @Lazy CameraLiveService cameraLiveService,
                           @Lazy CustomerCameraPermissionService customerCameraPermissionService,
                           EndUserDao endUserDao, CLTenantDao clTenantDao) {
    this.cameraDao = cameraDao;
    this.attributesDao = attributesDao;
    this.timeseriesLatestDao = timeseriesLatestDao;
    this.cameraLiveService = cameraLiveService;
    this.customerCameraPermissionService = customerCameraPermissionService;
    this.endUserDao = endUserDao;
    this.clTenantDao = clTenantDao;
  }

  @Override
  public PageData<CameraDto> getPageCameraBySearch(Pageable pageable, String textSearch, UUID boxId, SecurityUser securityUser)
      throws ExecutionException, InterruptedException, ThingsboardException, JSONException {
    PageData<CameraDto> cameraDtoPageData = cameraDao.getPageCamera(pageable, textSearch, boxId, securityUser.getTenantId().getId());
    for (CameraDto cameraDetailDto : cameraDtoPageData.getData()) {
      AttributeKvEntry resolutionAttribute =
          findAttribute(securityUser.getTenantId(), new DeviceId(cameraDetailDto.getTbDeviceId()),
              CameraInfoKeyConstant.RESOLUTION, DataConstants.CLIENT_SCOPE);
      if (resolutionAttribute != null) {
        cameraDetailDto.setResolution(resolutionAttribute.getValueAsString());
      }
    }
    return cameraDtoPageData;
  }

  @Override
  public CameraStreamGroup getCameraStreamByIdGroup(UUID groupId, String searchText, SecurityUser securityUser) throws JSONException,
      ExecutionException, InterruptedException, JsonProcessingException, ThingsboardException {
    boolean isTenantAdmin = true;
    boolean checkUserExist = endUserDao.existsByTenantIdAndUserId(securityUser.getTenantId().getId(), securityUser.getUuidId());
    if (checkUserExist) {
      EndUserDto endUserDto = endUserDao.getByUserId(securityUser.getUuidId(), securityUser);
      isTenantAdmin = clTenantDao.isTenantUser(endUserDto.getId());
    }
    CameraStreamGroup cameraStreamGroup = cameraDao.getCameraStreamByGroup(groupId, searchText, securityUser);
    boolean finalIsTenantAdmin = isTenantAdmin;
    cameraStreamGroup.setListCamera(cameraStreamGroup.getListCamera().stream()
        .filter(cameraDetailDto -> customerCameraPermissionService.checkViewLivePermission(securityUser, cameraDetailDto.getId())
            || finalIsTenantAdmin)
        .collect(Collectors.toList()));
    for (CameraDetailDto cameraDetailDto : cameraStreamGroup.getListCamera()) {
      addMoreInfo(cameraDetailDto);
      cameraLiveService.setCameraOnline(cameraDetailDto.getId(), securityUser);
    }
    return cameraStreamGroup;
  }

  @Override
  public List<CameraStreamGroup> getListCameraGroup(String searchText, SecurityUser securityUser) throws JSONException, ExecutionException,
      InterruptedException, JsonProcessingException, ThingsboardException {
    boolean isTenantAdmin = true;
    boolean checkUserExist = endUserDao.existsByTenantIdAndUserId(securityUser.getTenantId().getId(), securityUser.getUuidId());
    if (checkUserExist) {
      EndUserDto endUserDto = endUserDao.getByUserId(securityUser.getUuidId(), securityUser);
      isTenantAdmin = clTenantDao.isTenantUser(endUserDto.getId());
    }
    List<CameraStreamGroup> cameraStreamGroups = cameraDao.getAllCameraWithGroup(searchText, securityUser);
    for (CameraStreamGroup cameraStreamGroup : cameraStreamGroups) {
      boolean finalIsTenantAdmin = isTenantAdmin;
      cameraStreamGroup.setListCamera(cameraStreamGroup.getListCamera().stream()
          .filter(cameraDetailDto -> customerCameraPermissionService.checkViewLivePermission(securityUser, cameraDetailDto.getId())
              || finalIsTenantAdmin)
          .collect(Collectors.toList()));
      for (CameraDetailDto cameraDetailDto : cameraStreamGroup.getListCamera()) {
        addMoreInfo(cameraDetailDto);
        cameraLiveService.setCameraOnline(cameraDetailDto.getId(), securityUser);
      }
    }
    return cameraStreamGroups;
  }

  @Override
  public CameraEditDto save(CameraEditDto cameraEditDto, SecurityUser securityUser) {
    return cameraDao.save(cameraEditDto, securityUser);
  }

  @Override
  public CameraEditDto update(CameraEditDto cameraEditDto, SecurityUser securityUser) {
    return cameraDao.update(cameraEditDto, securityUser);
  }

  @Override
  public CameraEditDto changeBox(CameraEditDto cameraEditDto, SecurityUser securityUser) {
    return cameraDao.changeBox(cameraEditDto, securityUser);
  }

  @Override
  public CameraEditDto getCameraEditDtoById(UUID id) {
    return cameraDao.getCameraEditById(id);
  }

  @Override
  public CameraEditDto getCameraEditDtoByTenantIdAndTbDeviceId(UUID tenantId, UUID tbDeviceId) {
    return cameraDao.getCameraEditByTenantIdAndTbDeviceId(tenantId, tbDeviceId);
  }

  @Override
  public CameraDetailDto getCameraDetailDtoById(UUID id) throws JSONException, ExecutionException, InterruptedException, JsonProcessingException {
    CameraDetailDto cameraDetailDto = cameraDao.getCameraDetailDtoById(id);
    if (cameraDetailDto != null) {
      addMoreInfo(cameraDetailDto);
      return cameraDetailDto;
    }
    return null;
  }


  @Override
  public void deleteById(UUID tenantId, UUID id) {
    cameraDao.deleteById(tenantId, id);
  }

  @Override
  public void deleteByBoxId(UUID tenantId, UUID boxId) {
    cameraDao.deleteByBoxId(tenantId, boxId);
  }

  @Override
  public boolean checkCameraIdInDevice(TenantId tenantId, DeviceId deviceId, UUID cameraId) throws ExecutionException, InterruptedException {
    AttributeKvEntry resolutionAttribute =
        findAttribute(tenantId, deviceId, CameraInfoKeyConstant.CAMERA_ID, DataConstants.CLIENT_SCOPE);
    if (resolutionAttribute != null) {
      return resolutionAttribute.getValueAsString().equals(cameraId.toString());
    }
    return false;
  }

  @Override
  public boolean existsByTbDeviceIdAndTenantId(UUID tbDeviceId, UUID tenantId) {
    return cameraDao.existsByTbDeviceIdAndTenantId(tbDeviceId, tenantId);
  }

  @Override
  public boolean existsByTbDeviceIdAndTenantIdAndBoxId(UUID tbDeviceId, UUID tenantId, UUID boxId) {
    return cameraDao.existsByTbDeviceIdAndTenantIdAndBoxId(tbDeviceId, tenantId, boxId);
  }

  @Override
  public boolean existsByTbDeviceIdAndTenantIdAndIdNot(UUID tbDeviceId, UUID tenantId, UUID id) {
    return cameraDao.existsByTbDeviceIdAndTenantIdAndIdNot(tbDeviceId, tenantId, id);
  }

  @Override
  public boolean existsByCameraNameAndTenantId(String cameraName, UUID tenantId) {
    return cameraDao.existsByCameraNameAndTenantId(cameraName, tenantId);
  }

  @Override
  public boolean existsByCameraNameAndTenantIdAndIdNot(String cameraName, UUID tenantId, UUID id) {
    return cameraDao.existsByCameraNameAndTenantIdAndIdNot(cameraName, tenantId, id);
  }

  @Override
  public void deleteFromGroup(UUID tenantId, UUID id) {
    cameraDao.deleteFromGroup(tenantId, id);
  }

  @Override
  public void addToGroup(UUID tenantId, UUID id, AddCameraGroupDto addCameraGroupDto) {
    cameraDao.addToGroup(tenantId, id, addCameraGroupDto);
  }

  private AttributeKvEntry findAttribute(TenantId tenantId, DeviceId deviceId, String key, String scope) throws ExecutionException, InterruptedException {
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

  private void addMoreInfo(CameraDetailDto cameraDetailDto) throws ExecutionException, InterruptedException, JSONException, JsonProcessingException {

    AttributeKvEntry channelAttribute =
        findAttribute(new TenantId(cameraDetailDto.getTenantId()), new DeviceId(cameraDetailDto.getTbDeviceId()),
            CameraInfoKeyConstant.CHANNEL, DataConstants.CLIENT_SCOPE);
    if (channelAttribute != null) {
      cameraDetailDto.setChannel(channelAttribute.getValueAsString());
    }

    AttributeKvEntry resolutionAttribute =
        findAttribute(new TenantId(cameraDetailDto.getTenantId()), new DeviceId(cameraDetailDto.getTbDeviceId()),
            CameraInfoKeyConstant.RESOLUTION, DataConstants.CLIENT_SCOPE);
    if (resolutionAttribute != null) {
      cameraDetailDto.setResolution(resolutionAttribute.getValueAsString());
    }

    AttributeKvEntry fpsAttribute =
        findAttribute(new TenantId(cameraDetailDto.getTenantId()), new DeviceId(cameraDetailDto.getTbDeviceId()),
            CameraInfoKeyConstant.FPS, DataConstants.CLIENT_SCOPE);
    if (fpsAttribute != null) {
      cameraDetailDto.setFps(Integer.parseInt(fpsAttribute.getValueAsString()));
    }

    AttributeKvEntry bitrateAttribute =
        findAttribute(new TenantId(cameraDetailDto.getTenantId()), new DeviceId(cameraDetailDto.getTbDeviceId()),
            CameraInfoKeyConstant.BITRATE, DataConstants.CLIENT_SCOPE);
    if (bitrateAttribute != null) {
      cameraDetailDto.setBitrate(Long.parseLong(bitrateAttribute.getValueAsString()));
    }

    AttributeKvEntry fishEyeAttribute =
        findAttribute(new TenantId(cameraDetailDto.getTenantId()), new DeviceId(cameraDetailDto.getTbDeviceId()),
            CameraInfoKeyConstant.FISHEYE, DataConstants.CLIENT_SCOPE);
    if (fishEyeAttribute != null) {
      cameraDetailDto.setFisheye(Boolean.parseBoolean(fishEyeAttribute.getValueAsString()));
    }

    AttributeKvEntry fishEyeSupportAttribute =
        findAttribute(new TenantId(cameraDetailDto.getTenantId()), new DeviceId(cameraDetailDto.getTbDeviceId()),
            CameraInfoKeyConstant.FISHEYE_SUPPORT, DataConstants.CLIENT_SCOPE);
    if (fishEyeSupportAttribute != null) {
      cameraDetailDto.setFisheyeSupport(Boolean.parseBoolean(fishEyeSupportAttribute.getValueAsString()));
    }

    AttributeKvEntry profileAttribute =
        findAttribute(new TenantId(cameraDetailDto.getTenantId()), new DeviceId(cameraDetailDto.getTbDeviceId()),
            CameraInfoKeyConstant.PROFILE, DataConstants.CLIENT_SCOPE);
    if (profileAttribute != null) {
      LinkedHashMap<String, CameraAdvanceSettingOption> optionSettingMap = new LinkedHashMap<>();
      String profileAttributeJson = profileAttribute.getValueAsString();
      try {
        List<CameraProfileSetting> cameraProfileSettings =
            mapper.readValue(profileAttributeJson, new TypeReference<>() {
            });
        List<String> channelSupport = new ArrayList<>();
        for (CameraProfileSetting cameraProfileSetting : cameraProfileSettings) {
          channelSupport.add(cameraProfileSetting.getChannel());
          CameraAdvanceSettingOption cameraAdvanceSettingOption = new CameraAdvanceSettingOption();
          cameraAdvanceSettingOption.setFpsSupport(generateListSelectFps(cameraProfileSetting.getFpsSupport().getMin(),
              cameraProfileSetting.getFpsSupport().getMax()));
          cameraAdvanceSettingOption.setBitrateSupport(generateSelectBitrate(cameraProfileSetting.getBitrateSupport().getMin(),
              cameraProfileSetting.getBitrateSupport().getMax()));
          cameraAdvanceSettingOption.setResolutionSupport(genareteSelectResolution(cameraProfileSetting.getResolutionSupport()));
          optionSettingMap.put(cameraProfileSetting.getChannel(), cameraAdvanceSettingOption);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (optionSettingMap.isEmpty()) {
        optionSettingMap.put("mainStream", new CameraAdvanceSettingOption());
        optionSettingMap.put("subStream", new CameraAdvanceSettingOption());
      }
      cameraDetailDto.setOptionSetting(optionSettingMap);
    }

    TsKvEntry statusTsKv =
        findLatestTelemetry(
            new TenantId(cameraDetailDto.getTenantId()),
            new DeviceId(cameraDetailDto.getTbDeviceId()),
            CameraInfoKeyConstant.STATUS);
    AttributeKvEntry activeAttribute =
        findAttribute(new TenantId(cameraDetailDto.getTenantId()),
            new DeviceId(cameraDetailDto.getBoxId()),
            CameraInfoKeyConstant.ACTIVE, DataConstants.SERVER_SCOPE);
    if (statusTsKv != null && activeAttribute != null) {
      boolean status;
      if (statusTsKv.getValue() == null) {
        status = false;
      } else {
        status = statusTsKv.getValueAsString().equals("1");
      }
      cameraDetailDto.setActive(status
          && Boolean.parseBoolean(activeAttribute.getValueAsString()));
    }
  }

  private List<Integer> generateListSelectFps(int min, int max) {
    List<Integer> fpsList = new ArrayList<>();
    while (min < max) {
      fpsList.add(min - min % 15);
      min += 15;
    }
    fpsList.add(max);
    return fpsList;
  }

  private List<Long> generateSelectBitrate(long min, long max) {
    List<Long> bitrateList = new ArrayList<>();
    while (min < max) {
      bitrateList.add(min - min % 10);
      min += 200;
    }
    bitrateList.add(max);
    return bitrateList;
  }

  private List<ConverResolutionSupport> genareteSelectResolution(List<ResolutionSupport> resolutionSupports) {
    List<ConverResolutionSupport> resolutionSupportList = new ArrayList<>();
    for (ResolutionSupport resolutionSupport : resolutionSupports) {
      ConverResolutionSupport converResolutionSupport = new ConverResolutionSupport();
      converResolutionSupport.setDisplayKey(resolutionSupport.getHeight() + "P");
      converResolutionSupport.setSettingValue(resolutionSupport);
      resolutionSupportList.add(converResolutionSupport);
    }
    return resolutionSupportList;
  }
}
