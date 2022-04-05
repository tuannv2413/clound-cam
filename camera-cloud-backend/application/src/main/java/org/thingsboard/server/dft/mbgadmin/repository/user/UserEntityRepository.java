package org.thingsboard.server.dft.mbgadmin.repository.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dft.enduser.entity.user.EndUserEntity;
import org.thingsboard.server.dft.mbgadmin.entity.user.UserMngEntity;

import java.util.UUID;

public interface UserEntityRepository extends JpaRepository<UserEntity, UUID> {

    @Query("select ex from UserEntity ex where LOWER(ex.searchText) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<UserMngEntity> findAllByName(Pageable pageable, @Param("searchText") String searchText);
}
