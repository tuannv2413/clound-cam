package org.thingsboard.server.dft.enduser.controller.example;


import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.controller.BaseController;
import org.thingsboard.server.dft.enduser.dto.example.ExampleDto;
import org.thingsboard.server.queue.util.TbCoreComponent;
import org.thingsboard.server.service.security.model.SecurityUser;

import javax.annotation.security.PermitAll;
import java.util.UUID;


@RestController
@TbCoreComponent
@RequestMapping("/api/noauth/example")
public class ExampleController extends BaseController {

  @PermitAll
  @GetMapping
  @ResponseBody
  @ApiOperation(value = "Lấy 1 page example entity",
      notes = "")
  public ResponseEntity<?> getAll(
      @RequestParam(name = "pageSize") int pageSize,
      @RequestParam(name = "page") int page,
      @RequestParam(required = false, defaultValue = "") String textSearch,
      @RequestParam(required = false, defaultValue = "createdTime") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
    try {
      Pageable pageable =
          PageRequest.of(page, pageSize, Sort.Direction.fromString(sortOrder), sortProperty);
      PageData<ExampleDto> pageData =
          exampleService.getPage(pageable, textSearch);
      return new ResponseEntity<>(checkNotNull(pageData), HttpStatus.OK);
    } catch (Exception e) {
      handleException(e);
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PermitAll
  @GetMapping("{id}")
  @ResponseBody
  @ApiOperation(value = "Lấy thông tin example entity theo id (UUID)",
      notes = "")
  public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
    try {
      return new ResponseEntity<>(checkNotNull(exampleService.getById(id)), HttpStatus.OK);
    } catch (Exception e) {
      handleException(e);
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PermitAll
  @PostMapping
  @ResponseBody
  @ApiOperation(value = "Thêm mới hoặc update(id != null) example entity",
      notes = "Hệ thông sẽ tự điều chỉnh createdTime, updatedTime, createdBy, updatedBy")
  public ResponseEntity<?> createOrUpdate(@RequestBody ExampleDto exampleDto) {
    try {
//      SecurityUser securityUser = getCurrentUser();
      SecurityUser fakeUser = new SecurityUser();
      fakeUser.setId(new UserId(UUID.randomUUID()));
      return new ResponseEntity<>(checkNotNull(exampleService.createOrUpdate(exampleDto, fakeUser)),
          exampleDto.getId() == null ? HttpStatus.CREATED : HttpStatus.OK);
    } catch (Exception e) {
      handleException(e);
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

  @PermitAll
  @DeleteMapping("{id}")
  @ResponseBody
  @ApiOperation(value = "Xóa example entity by id (UUID)",
      notes = "")
  public ResponseEntity<?> deleteById(@PathVariable("id") UUID id) {
    try {
      exampleService.deleteById(id);
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      handleException(e);
      return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
    }
  }

}
