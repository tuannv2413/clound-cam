package org.thingsboard.server.dft.enduser.repository.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.enduser.entity.user.EndUserEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EndUserRepository extends JpaRepository<EndUserEntity, UUID> {

  @Query("select ex from EndUserEntity ex where (LOWER(ex.name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
      " OR LOWER(ex.phone) LIKE LOWER(CONCAT('%', :searchText, '%')))" +
      " and ex.delete = false and ex.tenantId =:tenantId")
  Page<EndUserEntity> findAllByName(Pageable pageable, @Param("searchText") String searchText, @Param("tenantId") UUID tenantId);

  @Query("select ex from EndUserEntity ex where (LOWER(ex.name) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
      " OR LOWER(ex.phone) LIKE LOWER(CONCAT('%', :searchText, '%')))" +
      " and ex.active =:active and ex.delete = false and ex.tenantId =:tenantId")
  Page<EndUserEntity> findAllByNameAndActive(Pageable pageable,
                                             @Param("searchText") String searchText,
                                             @Param("active") boolean active, @Param("tenantId") UUID tenantId);

  EndUserEntity findByPhoneAndDelete(String phone, boolean delete);

  @Query("select ex from EndUserEntity ex where ex.userId = :userId")
  Optional<EndUserEntity> findByUserId(@Param("userId") UUID userId);

  @Query("select e from EndUserEntity e where e.id = ?1 and e.tenantId = ?2")
  Optional<EndUserEntity> findByIdAndTenantId(UUID id, UUID tenantId);

  @Query("select ex from EndUserEntity ex where ex.phone =:phone and ex.delete = false and ex.id <> :userId")
  EndUserEntity findByPhoneAndDifferentUserId(@Param("phone") String phone, @Param("userId") UUID userId);

  @Query("select ex from EndUserEntity ex where ex.phone =:phone and ex.delete = false")
  EndUserEntity findByPhone(@Param("phone") String phone);

  @Query("select ex from EndUserEntity ex where ex.email =:email and ex.delete = false")
  EndUserEntity findByEmail(@Param("email") String email);

  EndUserEntity findByEmailIgnoreCaseAndDeleteTrue(String email);

  EndUserEntity findByPhoneAndDeleteTrue(String email);

  EndUserEntity findByEmailIgnoreCaseAndPhoneAndDeleteTrue(String email, String phone);

  @Query("select ex from EndUserEntity ex where ex.email =:email and ex.phone =:phone and ex.delete = true ")
  EndUserEntity findByEmailAndPhone(@Param("email") String email, @Param("phone") String phone);

  @Query("select u from EndUserEntity u " +
      " where u.email =:email and u.id <> :id and u.delete = false")
  EndUserEntity findByEmailAndDifferentId(@Param("email") String email, @Param("id") UUID id);

  @Query(value = "select count(c) > 0 from cl_user c where c.tenant_id = ?1 and c.tb_user_id = ?2", nativeQuery = true)
  boolean existsByTenantIdAndUserId(UUID tenantId, UUID tbUserId);
}
