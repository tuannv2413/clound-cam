/**
 * Copyright © 2016-2021 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.service.telemetry;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.ApiUsageRecordKey;
import org.thingsboard.server.common.data.alarm.Alarm;
import org.thingsboard.server.common.data.alarm.AlarmInfo;
import org.thingsboard.server.common.data.alarm.AlarmQuery;
import org.thingsboard.server.common.data.alarm.AlarmSearchStatus;
import org.thingsboard.server.common.data.alarm.AlarmSeverity;
import org.thingsboard.server.common.data.alarm.AlarmStatus;
import org.thingsboard.server.common.data.id.AlarmId;
import org.thingsboard.server.common.data.id.CustomerId;
import org.thingsboard.server.common.data.id.EntityId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.query.AlarmData;
import org.thingsboard.server.common.data.query.AlarmDataQuery;
import org.thingsboard.server.common.msg.queue.ServiceType;
import org.thingsboard.server.common.msg.queue.TbCallback;
import org.thingsboard.server.common.msg.queue.TopicPartitionInfo;
import org.thingsboard.server.dao.alarm.AlarmOperationResult;
import org.thingsboard.server.dao.alarm.AlarmService;
import org.thingsboard.server.dft.enduser.dto.clAlarm.ClALarmTimeRangeDto;
import org.thingsboard.server.dft.enduser.dto.clAlarm.ClAlarmRespDto;
import org.thingsboard.server.dft.enduser.dto.clAlarmHistory.ClAlarmHistoryDto;
import org.thingsboard.server.dft.enduser.service.camera.CameraService;
import org.thingsboard.server.dft.enduser.service.clAlarm.ClAlarmService;
import org.thingsboard.server.dft.enduser.service.clAlarmHistory.ClAlarmHistoryService;
import org.thingsboard.server.dft.util.constant.ClAlarmConstant;
import org.thingsboard.server.dft.util.constant.ClDeviceConstant;
import org.thingsboard.server.gen.transport.TransportProtos;
import org.thingsboard.server.queue.discovery.PartitionService;
import org.thingsboard.server.queue.usagestats.TbApiUsageClient;
import org.thingsboard.server.service.apiusage.TbApiUsageStateService;
import org.thingsboard.server.cluster.TbClusterService;
import org.thingsboard.server.service.subscription.SubscriptionManagerService;
import org.thingsboard.server.service.subscription.TbSubscriptionUtils;

import java.util.*;

/**
 * Created by ashvayka on 27.03.18.
 */
@Service
@Slf4j
public class DefaultAlarmSubscriptionService extends AbstractSubscriptionService implements AlarmSubscriptionService {

    private final AlarmService alarmService;
    private final TbApiUsageClient apiUsageClient;
    private final TbApiUsageStateService apiUsageStateService;
    // dftcustom
    private final ClAlarmService clAlarmService;
    private final CameraService cameraService;
    private final ClAlarmHistoryService clAlarmHistoryService;

    public DefaultAlarmSubscriptionService(TbClusterService clusterService,
                                           PartitionService partitionService,
                                           AlarmService alarmService,
                                           TbApiUsageClient apiUsageClient,
                                           TbApiUsageStateService apiUsageStateService,
                                           @Lazy ClAlarmService clAlarmService,
                                           @Lazy CameraService cameraService,
                                           @Lazy ClAlarmHistoryService clAlarmHistoryService) {
        super(clusterService, partitionService);
        this.alarmService = alarmService;
        this.apiUsageClient = apiUsageClient;
        this.apiUsageStateService = apiUsageStateService;
        this.clAlarmService = clAlarmService;
        this.cameraService = cameraService;
        this.clAlarmHistoryService = clAlarmHistoryService;
    }

    @Autowired(required = false)
    public void setSubscriptionManagerService(Optional<SubscriptionManagerService> subscriptionManagerService) {
        this.subscriptionManagerService = subscriptionManagerService;
    }

    @Override
    String getExecutorPrefix() {
        return "alarm";
    }

    @Override
    public Alarm createOrUpdateAlarm(Alarm alarm) {
        // dft custom
        /*
            todo:
                - check clAlarm active = false => return
                - check không vi phạm timeAlarmSetting => return
         */
        ClAlarmRespDto clAlarmRespDto = clAlarmService.getById(UUID.fromString(alarm.getType()));
        if(!clAlarmRespDto.isActive()){
            return alarm;
        }
        // check vi pham timeAlarmSetting - k vi pham => return alarm
        Map<String, List<ClALarmTimeRangeDto>> timeAlarmSettings =
                clAlarmRespDto.getTimeAlarmSetting();
        try {
            if (!validateAlarmTime(timeAlarmSettings)) {
                return alarm;
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        AlarmOperationResult result = alarmService.createOrUpdateAlarm(alarm, apiUsageStateService.getApiUsageState(alarm.getTenantId()).isAlarmCreationEnabled());
        if (result.isSuccessful()) {
            onAlarmUpdated(result);
        }
        if (result.isCreated()) {
            apiUsageClient.report(alarm.getTenantId(), null, ApiUsageRecordKey.CREATED_ALARMS_COUNT);
        }

        // là tạo mới => insert vào bảng cl_alarm_history
        if (result.isCreated()) {
            try{
                Alarm savedAlarm = result.getAlarm();
                ClAlarmHistoryDto clAlarmHistoryDto = new ClAlarmHistoryDto();
                clAlarmHistoryDto.setTenantId(savedAlarm.getTenantId().getId());
                clAlarmHistoryDto.setTbAlarmId(savedAlarm.getId().getId());
                clAlarmHistoryDto.setTbDeviceId(savedAlarm.getOriginator().getId());

                boolean isCamera = cameraService.existsByTbDeviceIdAndTenantId(savedAlarm.getOriginator().getId(),
                        savedAlarm.getTenantId().getId());
                String deviceType = isCamera ? ClDeviceConstant.DEVICE_TYPE_CAM : ClDeviceConstant.DEVICE_TYPE_BOX;

                clAlarmHistoryDto.setDeviceType(deviceType);
                clAlarmHistoryDto.setType(clAlarmRespDto.getType());
                clAlarmHistoryDto.setViewed(false);
                clAlarmHistoryDto.setCreatedTime(System.currentTimeMillis());
                clAlarmHistoryService.save(clAlarmHistoryDto);

                // send email, notify... warning
                clAlarmService.sendWarningAsync(savedAlarm.getTenantId().getId(), clAlarmHistoryDto.getCreatedTime(), clAlarmHistoryDto.getTbDeviceId(), clAlarmHistoryDto.getDeviceType(), clAlarmRespDto);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        return result.getAlarm();
    }

    @Override
    public Boolean deleteAlarm(TenantId tenantId, AlarmId alarmId) {
        AlarmOperationResult result = alarmService.deleteAlarm(tenantId, alarmId);
        onAlarmDeleted(result);
        return result.isSuccessful();
    }

    @Override
    public ListenableFuture<Boolean> ackAlarm(TenantId tenantId, AlarmId alarmId, long ackTs) {
        ListenableFuture<AlarmOperationResult> result = alarmService.ackAlarm(tenantId, alarmId, ackTs);
        Futures.addCallback(result, new AlarmUpdateCallback(), wsCallBackExecutor);
        return Futures.transform(result, AlarmOperationResult::isSuccessful, wsCallBackExecutor);
    }

    @Override
    public ListenableFuture<Boolean> clearAlarm(TenantId tenantId, AlarmId alarmId, JsonNode details, long clearTs) {
        ListenableFuture<AlarmOperationResult> result = clearAlarmForResult(tenantId, alarmId, details, clearTs);
        return Futures.transform(result, AlarmOperationResult::isSuccessful, wsCallBackExecutor);
    }

    @Override
    public ListenableFuture<AlarmOperationResult> clearAlarmForResult(TenantId tenantId, AlarmId alarmId, JsonNode details, long clearTs) {
        ListenableFuture<AlarmOperationResult> result = alarmService.clearAlarm(tenantId, alarmId, details, clearTs);
        Futures.addCallback(result, new AlarmUpdateCallback(), wsCallBackExecutor);
        return result;
    }

    @Override
    public ListenableFuture<Alarm> findAlarmByIdAsync(TenantId tenantId, AlarmId alarmId) {
        return alarmService.findAlarmByIdAsync(tenantId, alarmId);
    }

    @Override
    public ListenableFuture<AlarmInfo> findAlarmInfoByIdAsync(TenantId tenantId, AlarmId alarmId) {
        return alarmService.findAlarmInfoByIdAsync(tenantId, alarmId);
    }

    @Override
    public ListenableFuture<PageData<AlarmInfo>> findAlarms(TenantId tenantId, AlarmQuery query) {
        return alarmService.findAlarms(tenantId, query);
    }

    @Override
    public ListenableFuture<PageData<AlarmInfo>> findCustomerAlarms(TenantId tenantId, CustomerId customerId, AlarmQuery query) {
        return alarmService.findCustomerAlarms(tenantId, customerId, query);
    }

    @Override
    public AlarmSeverity findHighestAlarmSeverity(TenantId tenantId, EntityId entityId, AlarmSearchStatus alarmSearchStatus, AlarmStatus alarmStatus) {
        return alarmService.findHighestAlarmSeverity(tenantId, entityId, alarmSearchStatus, alarmStatus);
    }

    @Override
    public PageData<AlarmData> findAlarmDataByQueryForEntities(TenantId tenantId, CustomerId customerId, AlarmDataQuery query, Collection<EntityId> orderedEntityIds) {
        return alarmService.findAlarmDataByQueryForEntities(tenantId, customerId, query, orderedEntityIds);
    }

    @Override
    public ListenableFuture<Alarm> findLatestByOriginatorAndType(TenantId tenantId, EntityId originator, String type) {
        return alarmService.findLatestByOriginatorAndType(tenantId, originator, type);
    }

    private void onAlarmUpdated(AlarmOperationResult result) {
        wsCallBackExecutor.submit(() -> {
            Alarm alarm = result.getAlarm();
            TenantId tenantId = result.getAlarm().getTenantId();
            for (EntityId entityId : result.getPropagatedEntitiesList()) {
                TopicPartitionInfo tpi = partitionService.resolve(ServiceType.TB_CORE, tenantId, entityId);
                if (currentPartitions.contains(tpi)) {
                    if (subscriptionManagerService.isPresent()) {
                        subscriptionManagerService.get().onAlarmUpdate(tenantId, entityId, alarm, TbCallback.EMPTY);
                    } else {
                        log.warn("Possible misconfiguration because subscriptionManagerService is null!");
                    }
                } else {
                    TransportProtos.ToCoreMsg toCoreMsg = TbSubscriptionUtils.toAlarmUpdateProto(tenantId, entityId, alarm);
                    clusterService.pushMsgToCore(tpi, entityId.getId(), toCoreMsg, null);
                }
            }
        });
    }

    private void onAlarmDeleted(AlarmOperationResult result) {
        wsCallBackExecutor.submit(() -> {
            Alarm alarm = result.getAlarm();
            TenantId tenantId = result.getAlarm().getTenantId();
            for (EntityId entityId : result.getPropagatedEntitiesList()) {
                TopicPartitionInfo tpi = partitionService.resolve(ServiceType.TB_CORE, tenantId, entityId);
                if (currentPartitions.contains(tpi)) {
                    if (subscriptionManagerService.isPresent()) {
                        subscriptionManagerService.get().onAlarmDeleted(tenantId, entityId, alarm, TbCallback.EMPTY);
                    } else {
                        log.warn("Possible misconfiguration because subscriptionManagerService is null!");
                    }
                } else {
                    TransportProtos.ToCoreMsg toCoreMsg = TbSubscriptionUtils.toAlarmDeletedProto(tenantId, entityId, alarm);
                    clusterService.pushMsgToCore(tpi, entityId.getId(), toCoreMsg, null);
                }
            }
        });
    }

    private class AlarmUpdateCallback implements FutureCallback<AlarmOperationResult> {
        @Override
        public void onSuccess(@Nullable AlarmOperationResult result) {
            onAlarmUpdated(result);
        }

        @Override
        public void onFailure(Throwable t) {
            log.warn("Failed to update alarm", t);
        }
    }

    private boolean validateAlarmTime(Map<String, List<ClALarmTimeRangeDto>> timeSettings){
        if(timeSettings == null){
            return true;
        }
        Long now = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Long time = now - c.getTimeInMillis();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        List<ClALarmTimeRangeDto> timeSetting;
        switch (dayOfWeek){
            case Calendar.MONDAY:
                timeSetting = timeSettings.get(ClAlarmConstant.T2);
                break;
            case Calendar.TUESDAY:
                timeSetting = timeSettings.get(ClAlarmConstant.T3);
                break;
            case Calendar.WEDNESDAY:
                timeSetting = timeSettings.get(ClAlarmConstant.T4);
                break;
            case Calendar.THURSDAY:
                timeSetting = timeSettings.get(ClAlarmConstant.T5);
                break;
            case Calendar.FRIDAY:
                timeSetting = timeSettings.get(ClAlarmConstant.T6);
                break;
            case Calendar.SATURDAY:
                timeSetting = timeSettings.get(ClAlarmConstant.T7);
                break;
            default:
                timeSetting = timeSettings.get(ClAlarmConstant.CN);
        }
        return checkTimeAlarm(time, timeSetting);
    }

    private boolean checkTimeAlarm(Long time, List<ClALarmTimeRangeDto> timeSetting){
        if(timeSetting == null){
            return false;
        }
        boolean result = timeSetting.stream().anyMatch(x -> time >= x.getStartTs() && time <= x.getEndTs());
        return result;
    }

}
