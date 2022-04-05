package org.thingsboard.server.dft.util.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.thingsboard.server.common.data.AdminSettings;
import org.thingsboard.server.dao.settings.AdminSettingsDao;
import org.thingsboard.server.dft.mbgadmin.dto.setting.AntMediaServerDto;
import org.thingsboard.server.dft.mbgadmin.dto.setting.ServerUrlDto;
import org.thingsboard.server.dft.util.constant.SystemConstant;

import java.util.Collections;
import java.util.Objects;

@Component
@Slf4j
public class AntMediaClient {

  private final ObjectMapper mapper;
  private final AdminSettingsDao adminSettingsDao;

  @Autowired
  public AntMediaClient(ObjectMapper mapper, AdminSettingsDao adminSettingsDao) {
    this.mapper = mapper;
    this.adminSettingsDao = adminSettingsDao;
  }

  public String getAntMeidaUrl() throws JsonProcessingException {
    AdminSettings adminSettings = adminSettingsDao.findByKey(null, SystemConstant.LIVE_STREAM_SERVER);
    JsonNode settingsJsonValue = adminSettings.getJsonValue();
    log.info("server stream setting value: " + settingsJsonValue.asText());
    AntMediaServerDto serverUrlDto = mapper.treeToValue(settingsJsonValue, AntMediaServerDto.class);
    return serverUrlDto.getHttpUrl();
  }

  public JSONObject createStreamId(String streamId)
      throws JsonProcessingException, JSONException {
    try {
      String urlBuilder = getAntMeidaUrl() +
          "/rest/v2/broadcasts/create";

      String urlTemplate =
          UriComponentsBuilder.fromHttpUrl(urlBuilder)
              .encode()
              .toUriString();

      JSONObject json = new JSONObject();
      json.put("hlsViewerCount", 0);
      json.put("mp4Enabled", 0);
      json.put("name", streamId);
      json.put("playListItemList", Collections.EMPTY_LIST);
      json.put("rtmpViewerCount", 0);
      json.put("streamId", streamId);
      json.put("type", "liveStream");
      json.put("webRTCViewerCount", 0);
      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "application/json");
      HttpEntity<?> entity = new HttpEntity<>(json.toString(), headers);
      RestTemplate restTemplate = new RestTemplate();
      HttpEntity<String> respose = restTemplate.exchange(urlTemplate, HttpMethod.POST, entity, String.class);
      return new JSONObject(respose.getBody());
    } catch (Exception e) {
      return null;
    }
  }

  public JSONObject getDetailsStreamById(String streamId)
      throws JsonProcessingException, JSONException {
    try {
      String urlBuilder = getAntMeidaUrl() +
          "/rest/v2/broadcasts" +
          "/" + streamId;

      String urlTemplate =
          UriComponentsBuilder.fromHttpUrl(urlBuilder)
              .encode()
              .toUriString();

      HttpHeaders headers = new HttpHeaders();
      headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
      HttpEntity<?> entity = new HttpEntity<>(headers);
      RestTemplate restTemplate = new RestTemplate();
      HttpEntity<String> response =
          restTemplate.exchange(urlTemplate, HttpMethod.GET, entity, String.class);
      JSONObject streamEntity = new JSONObject(Objects.requireNonNull(response.getBody()));
      String status = streamEntity.getString("status");

//    return Objects.equals(status, "broadcasting");
      return streamEntity;
    } catch (Exception e) {
      return null;
    }
  }

  public void deleteStream(String streamId)
      throws JsonProcessingException {
    try {
      String urlTemplate =
          UriComponentsBuilder.fromHttpUrl(getAntMeidaUrl() + "/rest/v2/broadcasts/" + streamId)
              .encode()
              .toUriString();

      HttpHeaders headers = new HttpHeaders();
      headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
      HttpEntity<?> entity = new HttpEntity<>(headers);
      RestTemplate restTemplate = new RestTemplate();
      HttpEntity<String> response = restTemplate.exchange(urlTemplate, HttpMethod.DELETE, entity, String.class);
      log.info(response.getBody());
    } catch (Exception e) {
      log.info(e.getMessage());
    }
  }
}
