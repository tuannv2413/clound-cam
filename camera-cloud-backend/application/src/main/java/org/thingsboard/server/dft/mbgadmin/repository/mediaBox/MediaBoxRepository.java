package org.thingsboard.server.dft.mbgadmin.repository.mediaBox;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.mbgadmin.entity.mediaBox.MediaBoxEntity;

import java.util.UUID;

@Repository
public interface MediaBoxRepository extends JpaRepository<MediaBoxEntity, UUID> {
  MediaBoxEntity findMediaBoxEntityById(UUID id);

  //    @Query("select mbox from MediaBoxEntity mbox")
  //chưa có filter theo product number, khách hàng, statusConnect
  @Query("SELECT a FROM MediaBoxEntity a WHERE " +
      "(LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
      "OR LOWER(a.partNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
      "OR LOWER(a.consignment) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
      "AND(:type LIKE '' OR LOWER(a.type) LIKE LOWER(:type)) " +
      "AND(:status LIKE '' OR LOWER(a.status) LIKE LOWER(:status)) " +
      "AND (:firmwareVersion LIKE '' OR LOWER(a.firmwareVersion) LIKE LOWER(:firmwareVersion)) "
  )
  Page<MediaBoxEntity> findAllByName(Pageable pageable, @Param("searchText") String searchText, @Param("type") String type, @Param("status") String status, @Param(("firmwareVersion")) String firmwareVersion);

  @Query("SELECT DISTINCT mb FROM MediaBoxEntity mb LEFT JOIN BoxEntity b ON mb.id = b.mediaBoxEntity.id " +
      "LEFT JOIN AttributeKvEntity ak ON b.tbDeviceId = ak.id.entityId WHERE " +
      "(LOWER(mb.serialNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
      "OR LOWER(mb.partNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
      "OR LOWER(mb.consignment) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
      "AND(:type LIKE '' OR LOWER(mb.type) LIKE LOWER(:type)) " +
      "AND(:status LIKE '' OR LOWER(mb.status) LIKE LOWER(:status)) " +
      "AND (:firmwareVersion LIKE '' OR LOWER(mb.firmwareVersion) LIKE LOWER(:firmwareVersion)) " +
      "AND (:active IS NULL OR (mb.status <> 'DA_BAN' and :active = false) OR " +
      "(ak.id.attributeKey = 'active' and ak.id.attributeType = 'SERVER_SCOPE' and ak.booleanValue = :active)) " +
      "AND (b.isDelete = false OR b.isDelete IS NULL)")
  Page<MediaBoxEntity> findAllBySearch(Pageable pageable, @Param("searchText") String searchText, @Param("type") String type,
                                       @Param("status") String status, @Param("firmwareVersion") String firmwareVersion,
                                       @Param("active") Boolean active);

  boolean existsBySerialNumber(String serialNumber);

  boolean existsBySerialNumberAndIdNot(String serialNumber, UUID id);

  boolean existsByOriginSerialNumberAndIdNot(String originSerialNumber, UUID id);

  boolean existsByOriginSerialNumber (String originSerialNumber);

  MediaBoxEntity findMediaBoxEntityBySerialNumber(String serialNumber);

  @Query("SELECT DISTINCT mb FROM MediaBoxEntity mb LEFT JOIN BoxEntity b ON mb.id = b.mediaBoxEntity.id " +
          "LEFT JOIN AttributeKvEntity ak ON b.tbDeviceId = ak.id.entityId WHERE " +
          "(LOWER(mb.serialNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
          "OR LOWER(mb.partNumber) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
          "OR LOWER(mb.consignment) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
          "AND (mb.status LIKE 'TAI_KHO' OR mb.status LIKE 'DAI_LY')" +
          "AND(:type LIKE '' OR LOWER(mb.type) LIKE LOWER(:type)) " +
          "AND(:status LIKE '' OR LOWER(mb.status) LIKE LOWER(:status)) " +
          "AND (:firmwareVersion LIKE '' OR LOWER(mb.firmwareVersion) LIKE LOWER(:firmwareVersion)) " +
          "AND (:active IS NULL OR (mb.status <> '1' and :active = false) OR " +
          "(ak.id.attributeKey = 'active' and ak.id.attributeType = 'SERVER_SCOPE' and ak.booleanValue = :active))" +
          "AND (b.isDelete = false OR b.isDelete IS NULL)")
  Page<MediaBoxEntity> findCustomBoxBySearch(Pageable pageable, @Param("searchText") String searchText, @Param("type") String type,
                                       @Param("status") String status, @Param("firmwareVersion") String firmwareVersion,
                                       @Param("active") Boolean active);

}
