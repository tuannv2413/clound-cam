package org.thingsboard.server.dft.mbgadmin.repository.clUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.thingsboard.server.dft.mbgadmin.entity.clUser.CLUserEntity;

import java.util.UUID;

@Repository
public interface CLUserRepository extends JpaRepository<CLUserEntity, UUID> {
}
