package org.thingsboard.server.dft.mbgadmin.repository.role;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.mbgadmin.entity.role.RoleEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    @Query("Select r from RoleEntity r LEFT JOIN UserRoleEntity ur on r.id = ur.roleId LEFT JOIN UserEntity u on ur.userId = u.id WHERE u.id =:userId ")
    List<RoleEntity> findRolesByUser(@Param("userId") UUID uuid);

    RoleEntity findRoleEntityById(UUID id);

    @Query("SELECT a FROM RoleEntity a WHERE " +
            "LOWER (a.roleName) LIKE LOWER (CONCAT('%', :search, '%')) " +
            "OR LOWER (a.note) LIKE LOWER (CONCAT('%', :search, '%'))"
    )
    Page<RoleEntity> findAllByNameAndNote(Pageable pageable, @Param("search") String search);

    @Query("SELECT a FROM RoleEntity a WHERE " +
            "LOWER (a.roleName) LIKE LOWER (CONCAT('%', :search, '%')) " +
            "OR LOWER (a.note) LIKE LOWER (CONCAT('%', :search, '%')) ORDER BY a.tbUserEntity.size DESC"
    )
    Page<RoleEntity> findAllByNameAndNoteOrderDESC(Pageable pageable, @Param("search") String search);

    @Query("SELECT a FROM RoleEntity a WHERE " +
            "LOWER (a.roleName) LIKE LOWER (CONCAT('%', :search, '%')) " +
            "OR LOWER (a.note) LIKE LOWER (CONCAT('%', :search, '%')) ORDER BY a.tbUserEntity.size ASC"
    )
    Page<RoleEntity> findAllByNameAndNoteOrderASC(Pageable pageable, @Param("search") String search);

    RoleEntity findRoleEntityByRoleName(String roleName);

    boolean existsRoleEntityByRoleName(String roleName);

    @Query(
            value ="DELETE FROM cl_role_and_permission WHERE cl_role_id = :id",
            nativeQuery = true)
    @Modifying
    void deletePermissionRoles(@Param("id")UUID id);
}
