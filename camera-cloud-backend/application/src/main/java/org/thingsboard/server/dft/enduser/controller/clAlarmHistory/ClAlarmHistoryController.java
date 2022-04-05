package org.thingsboard.server.dft.enduser.controller.clAlarmHistory;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.enduser.dto.clAlarmHistory.ClAlarmHistoryInfoDto;
import org.thingsboard.server.dft.enduser.dto.clAlarmHistory.ClAlarmHistoryMarkViewedDto;
import org.thingsboard.server.queue.util.TbCoreComponent;

import javax.annotation.security.PermitAll;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@TbCoreComponent
@RequestMapping("/api/clAlarmHistory")
public class ClAlarmHistoryController extends BaseController {

    @PermitAll
    @GetMapping
    @ApiOperation(value = "Lấy danh sách cảnh báo với phân trang",
            notes = "type: loaicb(CONNECT, DISCONNECT, MOVING); boxTbDeviceId: id của tb device ứng với box; sortProperty: 'type', 'createdTime'; mặc định nếu sortProperty hoặc sortOrder = null thì sẽ sắp xếp theo viewed và createdTime")
    public ResponseEntity<?> getAllWithPaging(
            @RequestParam(name = "pageSize") int pageSize,
            @RequestParam(name = "page") int page,
            @RequestParam(required = false, defaultValue = "") String textSearch, // ten cam, box
            @RequestParam(required = false) String sortProperty,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) String type, // loai cb,
            @RequestParam(required = false) UUID boxTbDeviceId,
            @RequestParam(required = false) Boolean viewed,
            @RequestParam(required = false) Long startTs,
            @RequestParam(required = false) Long endTs
    ) throws ThingsboardException {
        try {
            UUID tenantId = getTenantId().getId();
            textSearch = textSearch.trim();
            Sort sort;
            if (sortOrder == null || sortProperty == null) {
                // sort by view and createdTime
                List<Sort.Order> order = new ArrayList<>();
                Sort.Order order1 = new Sort.Order(Sort.Direction.ASC, "viewed");
                Sort.Order order2 = new Sort.Order(Sort.Direction.DESC, "createdTime");
                order.add(order1);
                order.add(order2);
                sort = Sort.by(order);
            } else {
                sort = sortOrder.equalsIgnoreCase("ASC")
                        ? Sort.by(new Sort.Order(Sort.Direction.ASC, sortProperty))
                        : Sort.by(new Sort.Order(Sort.Direction.DESC, sortProperty));
            }
            Pageable pageable =
                    PageRequest.of(page, pageSize, sort);
            PageData<ClAlarmHistoryInfoDto> pageData = clAlarmHistoryService
                    .getAllWithPaging(pageable, textSearch, type, boxTbDeviceId, viewed, startTs, endTs, tenantId);
            return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @GetMapping("/count-not-view")
    @ApiOperation(value = "Đếm số lượng cảnh báo chưa xem", notes = "")
    public ResponseEntity<?> countTotalNotViewAlarm() throws ThingsboardException {
        try {
            UUID tenantId = getTenantId().getId();
            return ResponseEntity.ok(checkNotNull(clAlarmHistoryService.countTotalNotViewAlarm(tenantId)));
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @PermitAll
    @PostMapping("/mark-alarm-viewed")
    @ApiOperation(value = "Đánh dấu cảnh báo là đã xem", notes = "alarmHistoryIds là danh sách id của cảnh báo cần đánh dấu là đã xem." +
            " Để = null nếu muốn đánh dấu tất cả")
    public ResponseEntity<?> markAlarmViewed(@RequestBody ClAlarmHistoryMarkViewedDto dto) throws ThingsboardException {
        try {
            UUID tenantId = getTenantId().getId();
            clAlarmHistoryService.markALarmViewed(dto.getAlarmHistoryIds(), tenantId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
