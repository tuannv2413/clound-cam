package org.thingsboard.server.dft.enduser.repository.box;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.dft.enduser.entity.box.BoxEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface BoxRepository extends JpaRepository<BoxEntity, UUID> {

  @Query("SELECT DISTINCT b FROM BoxEntity b INNER JOIN AttributeKvEntity ak ON b.tbDeviceId = ak.id.entityId " +
      "LEFT JOIN AttributeKvEntity ak2 ON b.tbDeviceId = ak2.id.entityId " +
      "WHERE b.tenantId = :tenantId " +
      "AND (:active IS NULL OR (ak.id.attributeKey = 'active' and ak.id.attributeType = 'SERVER_SCOPE' and ak.booleanValue = :active)) " +
      "AND (ak2.id.attributeKey = 'model' OR ak2.id.attributeKey = 'ipv4') " +
      "AND ak2.id.attributeType = 'CLIENT_SCOPE' " +
      "AND (LOWER(b.boxName) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
      "OR LOWER(b.serialNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
      "OR LOWER(ak2.strValue) LIKE LOWER(CONCAT('%', :searchText, '%'))) and b.isDelete = false")
  Page<BoxEntity> findAllBySearchInfo(Pageable pageable, @Param("active") Boolean active, @Param("searchText") String searchText, @Param("tenantId") UUID tenantId);

  @Query("SELECT DISTINCT b FROM BoxEntity b " +
          "WHERE b.tenantId = :tenantId " +
          "AND (LOWER(b.boxName) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
          "OR LOWER(b.serialNumber) LIKE LOWER(CONCAT('%', :searchText, '%'))) and b.isDelete = false")
  Page<BoxEntity> findAllBySearchInfoCompact(Pageable pageable, @Param("searchText") String searchText, @Param("tenantId") UUID tenantId);

  List<BoxEntity> findAllByTenantIdAndIsDeleteFalse(UUID tenantId, Sort sort);

  boolean existsBySerialNumberAndIsDeleteFalse(String serialNumber);

  BoxEntity findDistinctBySerialNumberAndIsDeleteFalse(String serialNumber);

  boolean existsByBoxNameAndTenantIdAndIsDeleteFalse(String boxName, UUID tenantId);

  boolean existsByBoxNameAndTenantIdAndIdNotAndIsDeleteFalse(String boxName, UUID tenantId, UUID id);

  BoxEntity findByTenantIdAndTbDeviceIdAndIsDeleteFalse(UUID tenantId, UUID tbDeviceId);

  @Modifying
  @Transactional
  @Query("update BoxEntity b set b.isDelete = true, b.tbDeviceId = null, b.mediaBoxEntity = null where b.id = :id")
  void deleteSoftById(@Param("id") UUID id);

  @Modifying
  @Transactional
  @Query("update BoxEntity b set b.isDelete = true, b.tbDeviceId = null, b.mediaBoxEntity = null where b.id = :id and b.tenantId = :tenantId")
  void deleteSoftByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);
}
