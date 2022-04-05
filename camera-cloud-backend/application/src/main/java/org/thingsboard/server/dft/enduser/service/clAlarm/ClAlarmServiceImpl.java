package org.thingsboard.server.dft.enduser.service.clAlarm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.thingsboard.rule.engine.api.MailService;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dao.clAlarm.ClAlarmDao;
import org.thingsboard.server.dft.enduser.dto.box.BoxDetailDto;
import org.thingsboard.server.dft.enduser.dto.camera.CameraEditDto;
import org.thingsboard.server.dft.enduser.dto.clAlarm.ClAlarmInfoDto;
import org.thingsboard.server.dft.enduser.dto.clAlarm.ClAlarmReqDto;
import org.thingsboard.server.dft.enduser.dto.clAlarm.ClAlarmRespDto;
import org.thingsboard.server.dft.enduser.dto.clAlarm.ClAlarmTimeSettingDto;
import org.thingsboard.server.dft.enduser.dto.notificationSetting.NotificationSettingDto;
import org.thingsboard.server.dft.enduser.dto.notifyToken.NotifyTokenDto;
import org.thingsboard.server.dft.enduser.service.box.BoxService;
import org.thingsboard.server.dft.enduser.service.camera.CameraService;
import org.thingsboard.server.dft.enduser.service.notificationSetting.NotificationSettingService;
import org.thingsboard.server.dft.enduser.service.notifyToken.NotifyTokenService;
import org.thingsboard.server.dft.util.constant.ClAlarmConstant;
import org.thingsboard.server.dft.util.constant.ClDeviceConstant;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ClAlarmServiceImpl implements ClAlarmService {

    private final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private final ClAlarmDao clAlarmDao;
    private final MailService mailService;
    private final CameraService cameraService;
    private final BoxService boxService;
    private final NotificationSettingService notificationSettingService;
    private final NotifyTokenService notifyTokenService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ClAlarmServiceImpl(ClAlarmDao clAlarmDao,
                              @Lazy MailService mailService,
                              @Lazy CameraService cameraService,
                              @Lazy BoxService boxService,
                              @Lazy NotificationSettingService notificationSettingService,
                              @Lazy NotifyTokenService notifyTokenService,
                              ObjectMapper objectMapper) {
        this.clAlarmDao = clAlarmDao;
        this.mailService = mailService;
        this.cameraService = cameraService;
        this.boxService = boxService;
        this.notificationSettingService = notificationSettingService;
        this.notifyTokenService = notifyTokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageData<ClAlarmInfoDto> getAllWithPaging(Pageable pageable, String textSearch, String type, UUID tenantId) {
        return clAlarmDao.getAllWithPaging(pageable, textSearch, type, tenantId);
    }

    @Override
    public ClAlarmRespDto getById(UUID id) {
        return clAlarmDao.getById(id);
    }

    @Override
    public ClAlarmRespDto save(ClAlarmReqDto clAlarmReqDto, String action, SecurityUser currentUser) throws ThingsboardException {
        return clAlarmDao.save(clAlarmReqDto, action, currentUser);
    }

    @Override
    public void deleteById(UUID id) {
        clAlarmDao.deleteById(id);
    }

    @Override
    public ClAlarmTimeSettingDto updateTimeAlarmSetting(ClAlarmTimeSettingDto dto) {
        return clAlarmDao.updateTimeAlarmSetting(dto);
    }

    @Async
    @Override
    public void sendWarningAsync(UUID tenantId, Long alarmTime, UUID tbDeviceId, String deviceType, ClAlarmRespDto clAlarmRespDto) {
        String deviceName = "";
        String action;
        String alarmTimeStr;

        if (deviceType.equals(ClDeviceConstant.DEVICE_TYPE_CAM)) {
            CameraEditDto cameraEditDto = cameraService.getCameraEditDtoByTenantIdAndTbDeviceId(tenantId, tbDeviceId);
            deviceName = cameraEditDto == null ? deviceName : cameraEditDto.getCameraName();
        } else {
            BoxDetailDto boxDetailDto = boxService.getBoxDetailByTenantIdAndTbDeviceId(tenantId, tbDeviceId);
            deviceName = boxDetailDto == null ? deviceName : boxDetailDto.getBoxName();
        }
        action = getActionByClAlarmType(clAlarmRespDto.getType());
        alarmTimeStr = df.format(alarmTime);

        if (clAlarmRespDto.isViaNotify()) {
            // logic: push notify to all user related to this cam or box. ( user co quyen sd cam, box )
            try {
                NotificationSettingDto notificationSetting = notificationSettingService.getNotificationSetting();
                if (notificationSetting != null) {
                    String apiUrl = notificationSetting.getFirebaseApiUrl();
                    String authKey = notificationSetting.getFirebaseAccessToken();
                    List<String> receiverKeys = new ArrayList<>();
                    if (clAlarmRespDto.getAlarmReceivers() != null) {
                        clAlarmRespDto.getAlarmReceivers().forEach(x -> {
                            List<NotifyTokenDto> notifyTokenDtos = notifyTokenService.findAllByTbUserId(x.getId());
                            notifyTokenDtos.forEach(n -> {
                                receiverKeys.add(n.getNotifyToken());
                            });
                        });
                    }
                    String title = getNotifyTitle(deviceName, action);
                    String body = getNotifyBody(alarmTimeStr);

                    if (receiverKeys.size() > 0) {
                        pushNotifyMulti(apiUrl, authKey, receiverKeys, title, body);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (clAlarmRespDto.isViaSms()) {
            // logic
        }
        if (clAlarmRespDto.isViaEmail()) {
            try {
                List<String> to = new ArrayList<>();
                String subject = getEmailSubject(action);
                String message = getEmailMessage(deviceName, action, alarmTimeStr);

                if (clAlarmRespDto.getAlarmReceivers() != null) {
                    clAlarmRespDto.getAlarmReceivers().forEach(x -> {
                        to.add(x.getEmail());
                    });
                }

                if (to.size() > 0) {
                    mailService.sendEmail(null, to, subject, message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void pushNotifyMulti(String apiUrl, String authKey, List<String> tokens, String title, String body) throws JSONException {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap httpHeaders = new LinkedMultiValueMap();
        httpHeaders.set(
                "Authorization", "key=" + authKey);
        httpHeaders.set("Content-Type", "application/json");

        JSONObject msg = new JSONObject();
        msg.put("title", title);
        msg.put("body", body);

        JSONObject json = new JSONObject();
        json.put("notification", msg);
        json.put("registration_ids", new JSONArray(tokens));

        HttpEntity<String> httpEntity = new HttpEntity<String>(json.toString(), httpHeaders);
        restTemplate.postForObject(apiUrl, httpEntity, String.class);
    }

    private String getEmailSubject(String action) {
        return MessageFormat.format(ClAlarmConstant.WARNING_EMAIL_SUBJECT, action);
    }

    private String getEmailMessage(String deviceName, String action, String alarmTime) {
        return MessageFormat.format(ClAlarmConstant.WARNING_EMAIL_MESSAGE, deviceName, action, alarmTime);
    }

    private String getNotifyTitle(String deviceName, String action) {
        return MessageFormat.format(ClAlarmConstant.WARNING_NOTIFY_TITLE, deviceName, action);
    }

    private String getNotifyBody(String alarmTime) {
        return MessageFormat.format(ClAlarmConstant.WARNING_NOTIFY_BODY, alarmTime);
    }

    private String getActionByClAlarmType(String type) {
        switch (type) {
            case ClAlarmConstant.TYPE_CONNECT:
                return "kết nối";
            case ClAlarmConstant.TYPE_DISCONNECT:
                return "mất kết nối";

            default:
                return "có chuyển động";
        }
    }

}
