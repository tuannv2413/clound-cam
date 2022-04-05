package org.thingsboard.server.dft.enduser.repository.example;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.enduser.entity.example.ExampleEntity;

import java.util.UUID;

@Repository
public interface ExampleRepository extends JpaRepository<ExampleEntity, UUID> {

  ExampleEntity findExampleEntityById(UUID id);

  @Query("select ex from ExampleEntity ex where LOWER(ex.name) LIKE LOWER(CONCAT('%', :searchText, '%'))")
  Page<ExampleEntity> findAllByName(Pageable pageable, @Param("searchText") String searchText);
}
