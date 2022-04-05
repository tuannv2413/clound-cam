package org.thingsboard.server.dft.enduser.dao.example;


import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dto.example.ExampleDto;

import java.util.UUID;

public interface ExampleDao {
  ExampleDto createOrUpdate(ExampleDto exampleDto);

  PageData<ExampleDto> getPage(Pageable pageable, String textSearch);

  ExampleDto getById(UUID id);

  void deleteById(UUID id);
}
