package org.thingsboard.server.dft.enduser.repository.camera;

import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.enduser.entity.camera.CameraEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Repository
public class CameraCustomRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Transactional
    public List<CameraEntity> findBy(UUID boxId, UUID groupId, String cameraName, UUID tenantId) {
        StringBuilder where = new StringBuilder();
        where.append(" and c.tenantId = ").append("'").append(tenantId).append("'");
        if (boxId != null) {
            where.append(" and c.boxEntity.id = ").append("'").append(boxId).append("'");
        }
        if (groupId != null) {
            where.append(" and c.cameraGroupId = ").append("'").append(groupId).append("'");
        }
        if (cameraName != null) {
            where.append(" and lower(c.cameraName) LIKE lower('%").append(cameraName.trim()).append("%')");
        }
        String sql = "select c from CameraEntity c where 1=1 " + where;
        Query query = entityManager.createQuery(sql);

        return query.getResultList();
    }
}