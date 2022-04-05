package org.thingsboard.server.dft.mbgadmin.repository.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dft.enduser.entity.user.EndUserEntity;
import org.thingsboard.server.dft.mbgadmin.entity.user.UserMngEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserMngRepository extends JpaRepository<UserMngEntity, UUID> {
    @Query(value = "SELECT DISTINCT cluser.* FROM cl_user cluser " +
            "LEFT JOIN tb_user tbuser ON tbuser.id = cluser.tb_user_id " +
            "LEFT JOIN cl_tbuser_and_role tbuserrole ON tbuserrole.tb_user_id = tbuser.id " +
            "LEFT JOIN cl_role clrole ON clrole.id = tbuserrole.cl_role_id " +
            "WHERE LOWER(cluser.search_text) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "AND (:active IS NULL OR cluser.active = CAST(CAST(:active AS CHARACTER VARYING) AS BOOLEAN)) " +
            "AND (:roleId IS NULL OR clrole.id = CAST(CAST(:roleId AS CHARACTER VARYING) AS UUID)) "+
            "AND cluser.delete = 'f' ", nativeQuery = true)
    Page<UserMngEntity> findAllByName(Pageable pageable, @Param("searchText") String searchText, @Param("active") Boolean active, @Param("roleId") UUID roleId);

    @Query("select ex from UserMngEntity ex WHERE " +
            "LOWER(ex.searchText) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "AND (:active IS NULL OR ex.active = :active) ")
    Page<UserMngEntity> filterAll(Pageable pageable, @Param("searchText") String searchText, @Param("active") Boolean active);

    List<UserMngEntity> findAllByEmailAndPhone(@Param("email") String email, @Param("phone") String phone);

    @Query("select u from UserMngEntity ex RIGHT JOIN UserEntity u ON ex.tbUserId = u.id  " +
            "WHERE  ex.phone = :userName OR  LOWER(u.email) = LOWER(:userName) " +
            "AND ex.active = true AND ex.delete = false ")
    UserEntity findUserByUserName(@Param("userName") String userName);

    @Query("select ex from UserMngEntity ex RIGHT JOIN UserEntity u ON ex.tbUserId = u.id  WHERE  u.id = :userId AND ex.delete = false   ")
    UserMngEntity findByRelationUserId(@Param("userId") UUID id);

    List<UserMngEntity> findByEmailAndPhone(@Param("email") String email, @Param("phone") String phone);

    List<UserMngEntity> findByTbUserId(@Param("tbUserId") UUID tbUserId);

    @Query(value = "SELECT COUNT(*) FROM cl_tbuser_and_role WHERE cl_role_id = :roleId",
            nativeQuery = true)
    Integer findUsersCountByRoleId(@Param("roleId") UUID roleId);

    @Query("select ex from UserMngEntity ex where ex.phone =:phone and ex.delete = false")
    UserMngEntity findByPhone(@Param("phone") String phone);

    @Query("select ex from UserMngEntity ex where ex.phone =:phone and ex.delete = false")
    List<UserMngEntity> findAllByPhone(@Param("phone") String phone);

    UserMngEntity findByTbUserIdAndActiveAndDelete(@Param("tbUserId") UUID tbUserId, @Param("active") boolean active, @Param("delete") boolean delete);
}
