package org.thingsboard.server.dft.mbgadmin.repository.clBox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.mbgadmin.entity.clBox.CLBoxEntity;

import java.util.UUID;

@Repository
public interface CLBoxRepository extends JpaRepository<CLBoxEntity, UUID> {
}
