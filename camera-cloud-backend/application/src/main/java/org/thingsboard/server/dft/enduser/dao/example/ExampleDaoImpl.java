package org.thingsboard.server.dft.enduser.dao.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dto.example.ExampleDto;
import org.thingsboard.server.dft.enduser.entity.example.ExampleEntity;
import org.thingsboard.server.dft.enduser.repository.example.ExampleRepository;

import java.util.Optional;
import java.util.UUID;

@Component
public class ExampleDaoImpl implements ExampleDao {

  private final ExampleRepository exampleRepository;

  @Autowired
  public ExampleDaoImpl(ExampleRepository exampleRepository) {
    this.exampleRepository = exampleRepository;
  }

  @Override
  public ExampleDto createOrUpdate(ExampleDto exampleDto) {
    ExampleEntity exampleEntity = new ExampleEntity();
    if (exampleDto.getId() != null) {
      Optional<ExampleEntity> optionalExampleEntity = exampleRepository.findById(exampleDto.getId());
      if (optionalExampleEntity.isPresent()) {
        exampleEntity = optionalExampleEntity.get();
        exampleEntity.setUpdatedTime(exampleDto.getUpdatedTime());
        exampleEntity.setUpdatedBy(exampleDto.getUpdatedBy());
      } else {
        exampleEntity.setId(exampleDto.getId());
        exampleEntity.setCreatedBy(exampleDto.getCreatedBy());
        exampleEntity.setCreatedTime(exampleDto.getCreatedTime());
      }
    } else {
      exampleEntity.setId(UUID.randomUUID());
      exampleEntity.setCreatedBy(exampleDto.getCreatedBy());
      exampleEntity.setCreatedTime(exampleDto.getCreatedTime());
    }
    exampleEntity.setName(exampleDto.getName());
    exampleEntity.setNumber(exampleDto.getNumber());
    exampleEntity.setChecked(exampleDto.isChecked());
    exampleEntity = exampleRepository.save(exampleEntity);
    ExampleDto exampleDtoSave = new ExampleDto(exampleEntity);
    return exampleDtoSave;
  }

  @Override
  public PageData<ExampleDto> getPage(Pageable pageable, String textSearch) {
    Page<ExampleDto> exampleDtoPage = exampleRepository.findAllByName(pageable, textSearch).map(ExampleDto::new);
    return new PageData<>(exampleDtoPage.getContent(), exampleDtoPage.getTotalPages(),
        exampleDtoPage.getTotalElements(), exampleDtoPage.hasNext());
  }

  @Override
  public ExampleDto getById(UUID id) {
    ExampleEntity exampleEntity;
    Optional<ExampleEntity> optionalExampleEntity = exampleRepository.findById(id);
    if (optionalExampleEntity.isPresent()) {
      exampleEntity = optionalExampleEntity.get();
      return new ExampleDto(exampleEntity);
    } else {
      return null;
    }
  }

  @Override
  public void deleteById(UUID id) {
    exampleRepository.deleteById(id);
  }
}
