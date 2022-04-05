package org.thingsboard.server.dft.enduser.service.example;

import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.page.PageData;

import org.thingsboard.server.dft.enduser.dto.example.ExampleDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.UUID;

public interface ExampleService {
  ExampleDto createOrUpdate(ExampleDto exampleDto, SecurityUser securityUser);

  PageData<ExampleDto> getPage(Pageable pageable, String textSearch);

  ExampleDto getById(UUID id);

  void deleteById(UUID id);
}
