package org.thingsboard.server.dft.enduser.controller.videoHistory;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.annotation.security.PermitAll;
import java.util.UUID;

@RestController
@TbCoreComponent
@RequestMapping("/api")
public class VideoHistoryController extends BaseController {

    @PermitAll
    @GetMapping("/noauth/videoHistory/listVideos")
    @ResponseBody
    @ApiOperation(value = "Lấy danh sách videoHistory", notes = "")
    public ResponseEntity<?> getListVideoHistorys(
            @RequestParam(required = false) UUID boxId,
            @RequestParam UUID camId,
            @RequestParam Long startTs,
            @RequestParam Long endTs) throws ThingsboardException {
        try {
            return new ResponseEntity<>(checkNotNull(videoHistoryService
                    .getListVideosInTimeRange(camId, startTs, endTs)), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @GetMapping("/videoHistory/listVideos")
    @ResponseBody
    @ApiOperation(value = "Lấy danh sách videoHistory version 2 - bỏ filter boxId, thêm quyền truy cập api", notes = "")
    public ResponseEntity<?> getListVideoHistorysV2(
            @RequestParam UUID camId,
            @RequestParam Long startTs,
            @RequestParam Long endTs) throws ThingsboardException {
        try {
            return new ResponseEntity<>(checkNotNull(videoHistoryService
                    .getListVideosInTimeRange(camId, startTs, endTs)), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
