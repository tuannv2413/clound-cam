package org.thingsboard.server.dft.enduser.controller.clAlarm;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.DeviceProfile;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.device.profile.DeviceProfileAlarm;
import org.thingsboard.server.common.data.edge.EdgeEventActionType;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.plugin.ComponentLifecycleEvent;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.enduser.dto.clAlarm.ClAlarmInfoDto;
import org.thingsboard.server.dft.enduser.dto.clAlarm.ClAlarmReqDto;
import org.thingsboard.server.dft.enduser.dto.clAlarm.ClAlarmRespDto;
import org.thingsboard.server.dft.enduser.dto.clAlarm.ClAlarmTimeSettingDto;
import org.thingsboard.server.dft.util.constant.ClAlarmConstant;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@TbCoreComponent
@RequestMapping("/api/clAlarm")
public class ClAlarmController extends BaseController {

    @PermitAll
    @GetMapping
    @ApiOperation(value = "Lấy danh sách thông báo với phân trang", notes = "")
    public ResponseEntity<?> getAllWithPaging(
            @RequestParam(name = "pageSize") int pageSize,
            @RequestParam(name = "page") int page,
            @RequestParam(required = false, defaultValue = "") String textSearch,
            @RequestParam(required = false, defaultValue = "createdTime") String sortProperty,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String type // loai cb
    ) throws ThingsboardException {
        try {
            UUID tenantId = getTenantId().getId();
            textSearch = textSearch.trim();
            Pageable pageable =
                    PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty);
            PageData<ClAlarmInfoDto> pageData = clAlarmService.getAllWithPaging(pageable, textSearch, type, tenantId);
            return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @GetMapping("/{id}")
    @ApiOperation(value = "Lấy chi tiết một thông báo", notes = "")
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) throws ThingsboardException {
        try {
            return new ResponseEntity<>(checkNotNull(clAlarmService.getById(id)), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @PostMapping
    @ApiOperation(value = "Thêm mới hoặc update(id != null) ClAlarm", notes = "")
    public ResponseEntity<?> save(@Valid @RequestBody ClAlarmReqDto clAlarmReqDto) throws ThingsboardException {
        try {
            UUID clAlarmId = clAlarmReqDto.getId();
            boolean isCreated = clAlarmId == null;

            clAlarmReqDto.setTenantId(getTenantId().getId());

            if (isCreated) {
                clAlarmId = UUID.randomUUID();
                clAlarmReqDto.setId(clAlarmId);

                // update device profile of all box device
                if (clAlarmReqDto.getBoxDeviceIds() != null) {
                    updateDeviceProfile(clAlarmReqDto.getBoxDeviceIds(), clAlarmReqDto.getDpAlarmForBox(),
                            ClAlarmConstant.ACTION_CREATE, clAlarmId);
                }

                // update device profile of all cam device
                if (clAlarmReqDto.getCamDeviceIds() != null) {
                    updateDeviceProfile(clAlarmReqDto.getCamDeviceIds(), clAlarmReqDto.getDpAlarmForCam(),
                            ClAlarmConstant.ACTION_CREATE, clAlarmId);
                }
            } else {
                // update
                ClAlarmRespDto currentClAlarmRespDto = clAlarmService.getById(clAlarmId);
                checkNotNull(currentClAlarmRespDto);

                // update device profile of all box device
                if (clAlarmReqDto.getBoxDeviceIds() != null) {
                    updateDeviceProfile(clAlarmReqDto.getBoxDeviceIds(), clAlarmReqDto.getDpAlarmForBox(),
                            ClAlarmConstant.ACTION_UPDATE, clAlarmId);
                }

                // update device profile of all cam device
                if (clAlarmReqDto.getCamDeviceIds() != null) {
                    updateDeviceProfile(clAlarmReqDto.getCamDeviceIds(), clAlarmReqDto.getDpAlarmForCam(),
                            ClAlarmConstant.ACTION_UPDATE, clAlarmId);
                }

                // remove dpAlarm from all device that get unchecked
                if (currentClAlarmRespDto.getDevices() != null) {
                    List<Device> uncheckedDevices = currentClAlarmRespDto.getDevices().stream().filter(x -> {
                        return !clAlarmReqDto.getBoxDeviceIds().contains(x.getId().getId())
                                && !clAlarmReqDto.getCamDeviceIds().contains(x.getId().getId());
                    }).collect(Collectors.toList());

                    for (Device d : uncheckedDevices) {
                        DeviceProfile deviceProfile = deviceProfileService.findDeviceProfileById(getTenantId(), d.getDeviceProfileId());
                        List<DeviceProfileAlarm> dpAlarms = deviceProfile.getProfileData().getAlarms();
                        if (dpAlarms == null) {
                            dpAlarms = new ArrayList<>();
                        }
                        // remove old dpAlarm
                        UUID finalId = clAlarmId;
                        dpAlarms = dpAlarms.stream().filter(x -> !x.getAlarmType().equals(finalId.toString()))
                                .collect(Collectors.toList());

                        deviceProfile.getProfileData().setAlarms(dpAlarms);

                        boolean isFirmwareChanged = false;
                        boolean isSoftwareChanged = false;

                        DeviceProfile savedDeviceProfile = checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfile));

                        tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
                        tbClusterService.broadcastEntityStateChangeEvent(deviceProfile.getTenantId(), deviceProfile.getId(), ComponentLifecycleEvent.UPDATED);

                        logEntityAction(savedDeviceProfile.getId(), savedDeviceProfile, null, ActionType.UPDATED, null);

                        otaPackageStateService.update(savedDeviceProfile, isFirmwareChanged, isSoftwareChanged);

                        sendEntityNotificationMsg(getTenantId(), savedDeviceProfile.getId(),
                                deviceProfile.getId() == null ? EdgeEventActionType.ADDED : EdgeEventActionType.UPDATED);
                    }
                }
            }

            // update data for custom all table (cl_alarm, cl_alarm_receiver, cl_alarm_tbdevice)
            ClAlarmRespDto result = isCreated ? clAlarmService.save(clAlarmReqDto, ClAlarmConstant.ACTION_CREATE, getCurrentUser())
                    : clAlarmService.save(clAlarmReqDto, ClAlarmConstant.ACTION_UPDATE, getCurrentUser());
            return new ResponseEntity<>(checkNotNull(result), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @DeleteMapping("/{id}")
    @ApiOperation(value = "Xoá một thông báo", notes = "")
    public ResponseEntity<?> deleteById(@PathVariable("id") UUID id) throws ThingsboardException {
        try {
            ClAlarmRespDto current = clAlarmService.getById(id);
            checkNotNull(current);

            List<Device> devices = current.getDevices();
            if (devices != null) {
                for (Device d : devices) {
                    // remove dpAlarm
                    DeviceProfile deviceProfile = deviceProfileService.findDeviceProfileById(getTenantId(), d.getDeviceProfileId());

                    List<DeviceProfileAlarm> dpAlarms = deviceProfile.getProfileData().getAlarms();
                    if (dpAlarms == null) {
                        dpAlarms = new ArrayList<>();
                    }

                    dpAlarms = dpAlarms.stream().filter(x -> !x.getAlarmType().equals(current.getId().toString()))
                            .collect(Collectors.toList());

                    deviceProfile.getProfileData().setAlarms(dpAlarms);

                    boolean isFirmwareChanged = false;
                    boolean isSoftwareChanged = false;

                    DeviceProfile savedDeviceProfile = checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfile));

                    tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
                    tbClusterService.broadcastEntityStateChangeEvent(deviceProfile.getTenantId(), deviceProfile.getId(), ComponentLifecycleEvent.UPDATED);

                    logEntityAction(savedDeviceProfile.getId(), savedDeviceProfile, null, ActionType.UPDATED, null);

                    otaPackageStateService.update(savedDeviceProfile, isFirmwareChanged, isSoftwareChanged);

                    sendEntityNotificationMsg(getTenantId(), savedDeviceProfile.getId(),
                            deviceProfile.getId() == null ? EdgeEventActionType.ADDED : EdgeEventActionType.UPDATED);
                }
            }

            clAlarmService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @PostMapping("/timeSetting")
    @ApiOperation(value = "Update time setting một thông báo", notes = "")
    public ResponseEntity<?> updateTimeAlarmSetting(@Valid @RequestBody ClAlarmTimeSettingDto dto) throws ThingsboardException {
        try {
            return ResponseEntity.ok(checkNotNull(clAlarmService.updateTimeAlarmSetting(dto)));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    /*
        todo :
            - remove old dpAlarm
            - add new dpAlarm
     */
    private void updateDeviceProfile(List<UUID> deviceIds, DeviceProfileAlarm newDpALarm, String action, UUID clAlarmId)
            throws ThingsboardException {
        for (UUID deviceId : deviceIds) {
            Device device = deviceService.findDeviceById(getTenantId(), new DeviceId(deviceId));
            DeviceProfile deviceProfile = deviceProfileService.findDeviceProfileById(getTenantId(), device.getDeviceProfileId());

            List<DeviceProfileAlarm> dpAlarms = deviceProfile.getProfileData().getAlarms();
            if (dpAlarms == null) {
                dpAlarms = new ArrayList<>();
            }

            if (action.equals(ClAlarmConstant.ACTION_UPDATE)) {
                // remove old dpAlarm
                dpAlarms = dpAlarms.stream().filter(x -> !x.getAlarmType().equals(clAlarmId.toString()))
                        .collect(Collectors.toList());
            }

            newDpALarm.setId(UUID.randomUUID().toString());
            newDpALarm.setAlarmType(clAlarmId.toString()); // id cua ban ghi trong bang cl_alarm
            dpAlarms.add(newDpALarm);

            deviceProfile.getProfileData().setAlarms(dpAlarms);

            boolean isFirmwareChanged = false;
            boolean isSoftwareChanged = false;

            DeviceProfile savedDeviceProfile = checkNotNull(deviceProfileService.saveDeviceProfile(deviceProfile));

            tbClusterService.onDeviceProfileChange(savedDeviceProfile, null);
            tbClusterService.broadcastEntityStateChangeEvent(deviceProfile.getTenantId(), deviceProfile.getId(), ComponentLifecycleEvent.UPDATED);

            logEntityAction(savedDeviceProfile.getId(), savedDeviceProfile, null, ActionType.UPDATED, null);

            otaPackageStateService.update(savedDeviceProfile, isFirmwareChanged, isSoftwareChanged);

            sendEntityNotificationMsg(getTenantId(), savedDeviceProfile.getId(),
                    deviceProfile.getId() == null ? EdgeEventActionType.ADDED : EdgeEventActionType.UPDATED);
        }
    }

}
