package org.thingsboard.server.dft.enduser.repository.camera;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.dft.enduser.entity.camera.CameraEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CameraRepository extends JpaRepository<CameraEntity, UUID> {


    @Query("SELECT c FROM CameraEntity c where c.tenantId = :tenantId " +
            "and (LOWER(c.cameraName) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "OR LOWER(c.ipv4) LIKE LOWER(CONCAT('%', :searchText, '%')))  and c.isDelete = false")
    Page<CameraEntity> findAllBySearchInfo(Pageable pageable, @Param("searchText") String searchText, @Param("tenantId") UUID tenantId);

    @Query("SELECT c FROM CameraEntity c where c.tenantId = :tenantId and c.boxEntity.id = :boxId " +
            "and (LOWER(c.cameraName) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "OR LOWER(c.ipv4) LIKE LOWER(CONCAT('%', :searchText, '%'))) and c.isDelete = false")
    Page<CameraEntity> findAllBySearchInfo(Pageable pageable, @Param("boxId") UUID boxId, @Param("searchText") String searchText, @Param("tenantId") UUID tenantId);

    @Transactional(rollbackFor = {Exception.class})
    @Modifying
    void deleteByTenantIdAndId(UUID tenantId, UUID id);

    @Transactional(rollbackFor = {Exception.class})
    @Modifying
    @Query("DELETE FROM CameraEntity c WHERE c.tenantId = :tenantId AND c.boxEntity.id = :boxId  and c.isDelete = false")
    void deleteByTenantIdAndBoxId(@Param("tenantId") UUID tenantId, @Param("boxId") UUID boxId);

    boolean existsByTbDeviceIdAndTenantIdAndIsDeleteFalse(UUID tbDeviceId, UUID tenantId);

    @Query(value = "select count(c) > 0 from cl_camera c where c.tb_device_id = ?1 and c.tenant_id = ?2 and c.box_id = ?3 " +
        "and c.is_delete = false", nativeQuery = true)
    boolean existsByTbDeviceIdAndTenantIdAndBoxIdAndIsDeleteFalse(UUID tbDeviceId, UUID tenantId, UUID boxId);

    @Query(value = "select count(c) > 0 from cl_camera c where c.tb_device_id = ?1 " +
            "and c.tenant_id = ?2 and c.id != ?3 and c.is_delete = false", nativeQuery = true)
    boolean existsByTbDeviceIdAndTenantIdAndIdNotAndIsDeleteFalse(UUID tbDeviceId, UUID tenantId, UUID id);

    boolean existsByCameraNameAndTenantIdAndIsDeleteFalse(String cameraName, UUID tenantId);

    boolean existsByCameraNameAndTenantIdAndIdNotAndIsDeleteFalse(String cameraName, UUID tenantId, UUID id);


    @Query("SELECT c FROM CameraEntity c where c.cameraGroupId = :cameraGroupId and c.isDelete = false")
    List<CameraEntity> findByCameraGroup(@Param("cameraGroupId") UUID cameraGroupId);

    @Query("SELECT c FROM CameraEntity c where c.cameraGroupId = :cameraGroupId")
    List<CameraEntity> findByCameraGroup1(@Param("cameraGroupId") UUID cameraGroupId);


    @Query("SELECT c FROM CameraEntity c where c.tenantId = :tenantId and c.isDelete = false")
    List<CameraEntity> findByTenantId(@Param("tenantId") UUID tenantId);

    @Query("SELECT c FROM CameraEntity c where c.cameraGroupId = :cameraGroupId " +
            "AND (LOWER(c.cameraName) LIKE LOWER(CONCAT('%', :searchText, '%'))) and c.isDelete = false")
    List<CameraEntity> findByCameraGroupWithSearch(@Param("cameraGroupId") UUID cameraGroupId, @Param("searchText") String searchText);

    List<CameraEntity> findByCameraGroupIdIsNullAndIsDeleteFalseOrderByCreatedTimeDesc();

    List<CameraEntity> findAllByTenantIdAndIsDelete(UUID tenantId, boolean isDelete);

    @Query("select c from CameraEntity c where c.tenantId = :tenantId and c.id = :id and c.isDelete = false")
    CameraEntity findByTenantIdAndId(@Param("tenantId") UUID tenantId, @Param("id") UUID id);

    @Query("select c from CameraEntity c where c.tenantId = :tenantId and c.id = :id and c.isDelete = false AND (LOWER(c.cameraName) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    CameraEntity findByTenantIdAndIdAndCameraName(@Param("tenantId") UUID tenantId, @Param("id") UUID id, @Param("searchText") String searchText);

    @Query("select c from CameraEntity c where c.tenantId = :tenantId and c.tbDeviceId = :tbDeviceId and c.isDelete = false")
    CameraEntity findByTenantIdAndTbDeviceId(@Param("tenantId") UUID tenantId, @Param("tbDeviceId") UUID tbDeviceId);

    @Query("select c from CameraEntity c where c.cameraName = ?1 and c.isDelete = false")
    CameraEntity findByCameraName(String cameraName);

    @Query("select c from CameraEntity c where c.cameraGroupId = ?1 and c.tenantId = ?2 and c.isDelete = false")
    List<CameraEntity> findByCameraGroupIdAndTenantId(UUID cameraGroupId, UUID tenantId);

    @Query("select c from CameraEntity c where c.cameraGroupId = ?1 and c.tenantId = ?2 and c.id = ?3 and c.isDelete = false")
    CameraEntity findByCameraGroupIdAndTenantIdaAndId(UUID cameraGroupId, UUID tenantId, UUID cameraId);

    @Query("select c from CameraEntity c where c.cameraGroupId = ?1 and c.tenantId = ?2 and c.isDelete = false")
    List<CameraEntity> findByCameraGroupIdAndTenantId(UUID cameraGroupId, UUID tenantId, Sort sort);

    @Query("select c from CameraEntity c where c.cameraGroupId = :id and LOWER(c.cameraName) LIKE LOWER(CONCAT('%', :cameraName, '%')) and c.tenantId = :tenantId and c.isDelete = false")
    List<CameraEntity> findByCameraGroupIdAndTenantIdaAndCameraName(@Param("id") UUID id, @Param("cameraName") String cameraName, @Param("tenantId") UUID tenantId);

    @Query("select c from CameraEntity c where LOWER(c.cameraName) LIKE LOWER(CONCAT('%', :cameraName, '%')) and c.tenantId = :tenantId and c.isDelete = false")
    List<CameraEntity> findByTenantIAndCameraName(@Param("cameraName") String cameraName, @Param("tenantId") UUID tenantId);

    @Modifying
    @Transactional
    @Query("update CameraEntity c set c.isDelete = true, c.tbDeviceId = null where c.id = :id")
    void deleteSoftById(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("update CameraEntity c set c.isDelete = true, c.tbDeviceId = null where c.id = :id and c.tenantId = :tenantId")
    void deleteSoftByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = "select * from cl_camera c where c.box_id = ?1 " +
        "and c.ipv4 LIKE CONCAT('%', ?2, '%') and c.is_delete = false", nativeQuery = true)
    CameraEntity existsByBoxIdAndIpv4(UUID boxId, String ipv4);
}
