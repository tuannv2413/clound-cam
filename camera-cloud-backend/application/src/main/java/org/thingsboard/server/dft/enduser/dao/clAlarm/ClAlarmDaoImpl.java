package org.thingsboard.server.dft.enduser.dao.clAlarm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dao.device.DeviceDao;
import org.thingsboard.server.dao.model.sql.DeviceEntity;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dao.user.UserDao;
import org.thingsboard.server.dft.enduser.dao.camera.CameraDao;
import org.thingsboard.server.dft.enduser.dto.clAlarm.*;
import org.thingsboard.server.dft.enduser.entity.clAlarm.ClAlarmEntity;
import org.thingsboard.server.dft.enduser.repository.clAlarm.ClAlarmRepository;
import org.thingsboard.server.dft.util.constant.ClAlarmConstant;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ClAlarmDaoImpl implements ClAlarmDao {

    private final Gson gson = new Gson();

    private final ClAlarmRepository clAlarmRepository;
    private final DeviceDao deviceDao;
    private final UserDao userDao;
    private final CameraDao cameraDao;

    @Autowired
    public ClAlarmDaoImpl(
            ClAlarmRepository clAlarmRepository,
            @Lazy DeviceDao deviceDao,
            @Lazy UserDao userDao,
            @Lazy CameraDao cameraDao) {
        this.clAlarmRepository = clAlarmRepository;
        this.deviceDao = deviceDao;
        this.userDao = userDao;
        this.cameraDao = cameraDao;
    }

    @Override
    public PageData<ClAlarmInfoDto> getAllWithPaging(Pageable pageable, String textSearch, String type, UUID tenantId) {
        Page<ClAlarmInfoDto> clAlarmInfoDtoPage = clAlarmRepository.findAllWithPaging(tenantId, textSearch, type, pageable).map(this::toClAlarmInfoDto);
        PageData<ClAlarmInfoDto> result = new PageData<>(clAlarmInfoDtoPage.getContent(), clAlarmInfoDtoPage.getTotalPages(),
                clAlarmInfoDtoPage.getTotalElements(), clAlarmInfoDtoPage.hasNext());
        return result;
    }

    @Override
    public ClAlarmRespDto getById(UUID id) {
        ClAlarmEntity clAlarmEntity = clAlarmRepository.findById(id).orElse(null);
        return clAlarmEntity == null ? null : toClAlarmRespDto(clAlarmEntity);
    }

    @Override
    public ClAlarmRespDto save(ClAlarmReqDto clAlarmReqDto, String action, SecurityUser currentUser) throws ThingsboardException {
        ClAlarmRespDto result;
        ClAlarmEntity newClAlarmEntity;
        ClAlarmEntity savedClAlarmEntity;
        ClAlarmEntity currentClAlarmEntity;

        if (action.equals(ClAlarmConstant.ACTION_CREATE)) {
            newClAlarmEntity = toClAlarmEntity(clAlarmReqDto);
            newClAlarmEntity.setCreatedBy(currentUser.getUuidId());
            newClAlarmEntity.setCreatedTime(System.currentTimeMillis());

            savedClAlarmEntity = clAlarmRepository.save(newClAlarmEntity);
            result = toClAlarmRespDto(savedClAlarmEntity);
        } else {
            currentClAlarmEntity = clAlarmRepository.findById(clAlarmReqDto.getId()).orElse(null);
            if (currentClAlarmEntity == null) {
                throw new ThingsboardException("Requested item wasn't found!", ThingsboardErrorCode.ITEM_NOT_FOUND);
            }

            currentClAlarmEntity.setAlarmName(clAlarmReqDto.getAlarmName());
            currentClAlarmEntity.setType(clAlarmReqDto.getType());
            currentClAlarmEntity.setViaNotify(clAlarmReqDto.isViaNotify());
            currentClAlarmEntity.setViaSms(clAlarmReqDto.isViaSms());
            currentClAlarmEntity.setViaEmail(clAlarmReqDto.isViaEmail());
            currentClAlarmEntity.setActive(clAlarmReqDto.isActive());
            currentClAlarmEntity.setTimeAlarmSetting(gson.toJson(clAlarmReqDto.getTimeAlarmSetting()));
            currentClAlarmEntity.setUpdatedBy(currentUser.getUuidId());
            currentClAlarmEntity.setUpdatedTime(System.currentTimeMillis());

            List<DeviceEntity> deviceEntities = new ArrayList<>();
            List<UserEntity> alarmReceivers = new ArrayList<>();

            if (clAlarmReqDto.getBoxDeviceIds() != null) {
                clAlarmReqDto.getBoxDeviceIds().forEach(x -> {
                    Device d = deviceDao.findById(new TenantId(clAlarmReqDto.getTenantId()), x);
                    DeviceEntity de = d == null ? null : new DeviceEntity(d);
                    if (de != null) {
                        deviceEntities.add(de);
                    }
                });
            }

            if (clAlarmReqDto.getCamDeviceIds() != null) {
                clAlarmReqDto.getCamDeviceIds().forEach(x -> {
                    Device d = deviceDao.findById(new TenantId(clAlarmReqDto.getTenantId()), x);
                    DeviceEntity de = d == null ? null : new DeviceEntity(d);
                    if (de != null) {
                        deviceEntities.add(de);
                    }
                });
            }

            if (clAlarmReqDto.getAlarmReceiverIds() != null) {
                clAlarmReqDto.getAlarmReceiverIds().forEach(x -> {
                    User user = userDao.findById(new TenantId(clAlarmReqDto.getTenantId()), x);
                    UserEntity ue = user == null ? null : new UserEntity(user);
                    if (ue != null) {
                        alarmReceivers.add(ue);
                    }
                });
            }

            currentClAlarmEntity.setDeviceEntities(deviceEntities);
            currentClAlarmEntity.setAlarmReceivers(alarmReceivers);

            savedClAlarmEntity = clAlarmRepository.save(currentClAlarmEntity);
            result = toClAlarmRespDto(savedClAlarmEntity);
        }
        return result;
    }

    @Override
    public void deleteById(UUID id) {
        clAlarmRepository.deleteById(id);
    }

    @Override
    public ClAlarmTimeSettingDto updateTimeAlarmSetting(ClAlarmTimeSettingDto dto) {
        ClAlarmTimeSettingDto result = null;
        ClAlarmEntity clAlarmEntity = clAlarmRepository.findById(dto.getAlarmId()).orElse(null);
        if (clAlarmEntity != null) {
            clAlarmEntity.setTimeAlarmSetting(gson.toJson(dto.getTimeAlarmSetting()));
            ClAlarmEntity savedClAlarmEntity = clAlarmRepository.save(clAlarmEntity);
            result = toClAlarmTimeSettingDto(savedClAlarmEntity);
        }
        return result;
    }

    private ClAlarmEntity toClAlarmEntity(ClAlarmReqDto clAlarmReqDto) {
        ClAlarmEntity result = new ClAlarmEntity();
        result.setId(clAlarmReqDto.getId());
        result.setTenantId(clAlarmReqDto.getTenantId());
        result.setAlarmName(clAlarmReqDto.getAlarmName());
        result.setType(clAlarmReqDto.getType());
        result.setViaNotify(clAlarmReqDto.isViaNotify());
        result.setViaSms(clAlarmReqDto.isViaSms());
        result.setViaEmail(clAlarmReqDto.isViaEmail());
        result.setActive(clAlarmReqDto.isActive());
        result.setTimeAlarmSetting(gson.toJson(clAlarmReqDto.getTimeAlarmSetting()));

        List<DeviceEntity> deviceEntities = new ArrayList<>();
        List<UserEntity> alarmReceivers = new ArrayList<>();

        if (clAlarmReqDto.getBoxDeviceIds() != null) {
            clAlarmReqDto.getBoxDeviceIds().forEach(x -> {
                Device d = deviceDao.findById(new TenantId(clAlarmReqDto.getTenantId()), x);
                DeviceEntity de = d == null ? null : new DeviceEntity(d);
                if (de != null) {
                    deviceEntities.add(de);
                }
            });
        }

        if (clAlarmReqDto.getCamDeviceIds() != null) {
            clAlarmReqDto.getCamDeviceIds().forEach(x -> {
                Device d = deviceDao.findById(new TenantId(clAlarmReqDto.getTenantId()), x);
                DeviceEntity de = d == null ? null : new DeviceEntity(d);
                if (de != null) {
                    deviceEntities.add(de);
                }
            });
        }

        if (clAlarmReqDto.getAlarmReceiverIds() != null) {
            clAlarmReqDto.getAlarmReceiverIds().forEach(x -> {
                User user = userDao.findById(new TenantId(clAlarmReqDto.getTenantId()), x);
                UserEntity ue = user == null ? null : new UserEntity(user);
                if (ue != null) {
                    alarmReceivers.add(ue);
                }
            });
        }

        result.setDeviceEntities(deviceEntities);
        result.setAlarmReceivers(alarmReceivers);

        return result;
    }

    private ClAlarmRespDto toClAlarmRespDto(ClAlarmEntity entity) {
        ClAlarmRespDto result = new ClAlarmRespDto();
        result.setId(entity.getId());
        result.setTenantId(entity.getTenantId());
        result.setAlarmName(entity.getAlarmName());
        result.setType(entity.getType());
        result.setViaNotify(entity.isViaNotify());
        result.setViaSms(entity.isViaSms());
        result.setViaEmail(entity.isViaEmail());
        result.setActive(entity.isActive());
        try {
            result.setTimeAlarmSetting(getMapFromStr(entity.getTimeAlarmSetting()));
        } catch (Exception e) {
            result.setTimeAlarmSetting(null);
        }

        List<Device> devices = new ArrayList<>();
        List<UUID> boxDeviceIds = new ArrayList<>();
        List<UUID> camDeviceIds = new ArrayList<>();
        List<ClAlarmUserDto> alarmReceivers = new ArrayList<>();
        List<UUID> alarmReceiverIds = new ArrayList<>();

        if (entity.getDeviceEntities() != null) {
            entity.getDeviceEntities().forEach(x -> {
                devices.add(x.toData());
            });
            devices.forEach(d -> {
                if (cameraDao.existsByTbDeviceIdAndTenantId(d.getUuidId(), d.getTenantId().getId())) {
                    camDeviceIds.add(d.getUuidId());
                } else {
                    boxDeviceIds.add(d.getUuidId());
                }
            });
        }

        if (entity.getAlarmReceivers() != null) {
            entity.getAlarmReceivers().forEach(x -> {
                ClAlarmUserDto c = new ClAlarmUserDto();
                c.setId(x.getId());
                c.setEmail(x.getEmail());
                alarmReceivers.add(c);
            });
        }
        alarmReceivers.forEach(r -> {
            alarmReceiverIds.add(r.getId());
        });

        result.setDevices(devices);
        result.setBoxDeviceIds(boxDeviceIds);
        result.setCamDeviceIds(camDeviceIds);
        result.setAlarmReceivers(alarmReceivers);
        result.setAlarmReceiverIds(alarmReceiverIds);

        return result;
    }

    private ClAlarmInfoDto toClAlarmInfoDto(ClAlarmEntity entity) {
        ClAlarmInfoDto result = new ClAlarmInfoDto();
        result.setId(entity.getId());
        result.setTenantId(entity.getTenantId());
        result.setAlarmName(entity.getAlarmName());
        result.setType(entity.getType());
        result.setViaNotify(entity.isViaNotify());
        result.setViaSms(entity.isViaSms());
        result.setViaEmail(entity.isViaEmail());
        result.setActive(entity.isActive());
        try {
            result.setTimeAlarmSetting(getMapFromStr(entity.getTimeAlarmSetting()));
        } catch (Exception e) {
            result.setTimeAlarmSetting(null);
        }
        TenantId tenantId = new TenantId(entity.getTenantId());
        result.setCreatedBy(entity.getCreatedBy());
        result.setCreatedTime(entity.getCreatedTime());
        result.setUpdatedBy(entity.getUpdatedBy());
        result.setUpdatedTime(entity.getUpdatedTime());
        if (entity.getCreatedBy() != null) {
            User createdBy = userDao.findById(tenantId, entity.getCreatedBy());
            result.setCreatedByStr(createdBy != null ? createdBy.getEmail() : null);
        }
        if (entity.getUpdatedBy() != null) {
            User updatedby = userDao.findById(tenantId, entity.getUpdatedBy());
            result.setUpdatedByStr(updatedby != null ? updatedby.getEmail() : null);
        }

        List<Device> devices = new ArrayList<>();
        List<ClAlarmUserDto> alarmReceivers = new ArrayList<>();

        if (entity.getDeviceEntities() != null) {
            entity.getDeviceEntities().forEach(x -> {
                devices.add(x.toData());
            });
        }

        if (entity.getAlarmReceivers() != null) {
            entity.getAlarmReceivers().forEach(x -> {
                ClAlarmUserDto c = new ClAlarmUserDto();
                c.setId(x.getId());
                c.setEmail(x.getEmail());
                alarmReceivers.add(c);
            });
        }

        result.setDevices(devices);
        result.setAlarmReceivers(alarmReceivers);

        // count cam, box
        Long numberOfCam = devices.stream().filter(x -> isCamera(x.getUuidId(), x.getTenantId().getId())).count();
        Long numberOfBox = devices.size() - numberOfCam;

        result.setNumberOfCam(numberOfCam);
        result.setNumberOfBox(numberOfBox);

        return result;
    }

    private ClAlarmTimeSettingDto toClAlarmTimeSettingDto(ClAlarmEntity entity){
        if(entity == null){
            return null;
        }
        ClAlarmTimeSettingDto result = new ClAlarmTimeSettingDto();
        result.setAlarmId(entity.getId());
        result.setTimeAlarmSetting(getMapFromStr(entity.getTimeAlarmSetting()));
        return result;
    }

    private boolean isCamera(UUID tbDeviceId, UUID tenantId) {
        return cameraDao.existsByTbDeviceIdAndTenantId(tbDeviceId, tenantId);
    }

    private Map<String, List<ClALarmTimeRangeDto>> getMapFromStr(String x) {
        Type empMapType = new TypeToken<Map<String, List<ClALarmTimeRangeDto>>>() {
        }.getType();
        return gson.fromJson(x, empMapType);
    }
}
