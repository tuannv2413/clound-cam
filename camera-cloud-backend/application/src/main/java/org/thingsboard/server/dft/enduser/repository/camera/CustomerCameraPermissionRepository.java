package org.thingsboard.server.dft.enduser.repository.camera;

import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.enduser.dto.camera.CustomerCameraPermissionDto;
import org.thingsboard.server.dft.util.JpaFieldCaster;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class CustomerCameraPermissionRepository {

    @PersistenceContext
    EntityManager entityManager;

    public List<CustomerCameraPermissionDto> findBy(UUID boxId, UUID groupId, UUID clUserId, String cameraName, UUID tenantId) {
        StringBuilder where = new StringBuilder();
        if (clUserId != null) {
            where.append(" and cc.clCameraId = ce.id");
        }
        where.append(" and ce.tenantId = ").append("'").append(tenantId).append("'");
        if (boxId != null) {
            where.append(" and ce.boxEntity.id = ").append("'").append(boxId).append("'");
        }
        if (groupId != null) {
            where.append(" and ce.cameraGroupId = ").append("'").append(groupId).append("'");
        }
        if (cameraName != null) {
            where.append(" and ce.cameraName LIKE '%").append(cameraName).append("%'");
        }
        String sql = "select cc.clUserId,cc.clCameraId,cc.live,cc.history,cc.ptz from CustomerCameraPermissionEntity cc, CameraEntity ce where 1=1" + where;

        Query query = entityManager.createQuery(sql);
        List<Object[]> rsList = query.getResultList();
        return rsList.stream().map(object -> CustomerCameraPermissionDto.builder()
                .userID(JpaFieldCaster.getUUID(object[0]))
                .clCameraId(JpaFieldCaster.getUUID(object[1]))
                .live(JpaFieldCaster.getBoolean(object[2]))
                .history(JpaFieldCaster.getBoolean(object[3]))
                .ptz(JpaFieldCaster.getBoolean(object[4]))
                .build()).collect(Collectors.toList());
    }

}