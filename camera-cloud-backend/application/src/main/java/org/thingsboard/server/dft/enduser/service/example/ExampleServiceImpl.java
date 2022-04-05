package org.thingsboard.server.dft.enduser.service.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dao.example.ExampleDao;
import org.thingsboard.server.dft.enduser.dto.example.ExampleDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.Date;
import java.util.UUID;

@Service
public class ExampleServiceImpl implements ExampleService {

  private final ExampleDao exampleDao;

  @Autowired
  public ExampleServiceImpl(ExampleDao exampleDao) {
    this.exampleDao = exampleDao;
  }

  @Override
  public ExampleDto createOrUpdate(ExampleDto exampleDto, SecurityUser securityUser) {
    exampleDto.setCreatedTime(new Date().getTime());
    exampleDto.setUpdatedTime(new Date().getTime());
    exampleDto.setCreatedBy(securityUser.getUuidId());
    exampleDto.setUpdatedBy(securityUser.getUuidId());
    return exampleDao.createOrUpdate(exampleDto);
  }

  @Override
  public PageData<ExampleDto> getPage(Pageable pageable, String textSearch) {
    return exampleDao.getPage(pageable, textSearch);
  }

  @Override
  public ExampleDto getById(UUID id) {
    return exampleDao.getById(id);
  }

  @Override
  public void deleteById(UUID id) {
    exampleDao.deleteById(id);
  }
}
