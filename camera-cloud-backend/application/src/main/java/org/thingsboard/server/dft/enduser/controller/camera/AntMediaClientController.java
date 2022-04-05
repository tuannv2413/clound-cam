package org.thingsboard.server.dft.enduser.controller.camera;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.util.service.AntMediaClient;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.annotation.security.PermitAll;

@Slf4j
@RestController
@TbCoreComponent
@RequestMapping("/api/ant-media-client")
public class AntMediaClientController extends BaseController {

  @Autowired
  AntMediaClient antMediaClient;

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
  @PostMapping("{streamId}")
  @ResponseBody
  @ApiOperation(value = "Api tạo stream với ant media",
      notes = "Truyền vào streamId")
  public ResponseEntity<?> createAntmediaStream(@PathVariable("streamId") String streamId) throws ThingsboardException {
    try {
      if (streamId == null) {
        streamId = RandomStringUtils.randomAlphabetic(12);
      }
      return new ResponseEntity<>(antMediaClient.createStreamId(streamId).toString(), HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }

  @PreAuthorize("hasAnyAuthority('SYS_ADMIN', 'TENANT_ADMIN', 'CUSTOMER_USER')")
  @DeleteMapping("{streamId}")
  @ResponseBody
  @ApiOperation(value = "Api delete stream với ant media",
      notes = "Truyền vào streamId")
  public ResponseEntity<?> deleteAntmediaStream(@PathVariable("streamId") String streamId) throws ThingsboardException {
    try {
      checkNotNull(streamId);
      antMediaClient.deleteStream(streamId);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      throw handleException(e);
    }
  }
}
