package org.thingsboard.server.dft.mbgadmin.dao.clTenant;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.dft.mbgadmin.dto.clTenant.*;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseDataDto;
import org.thingsboard.server.dft.mbgadmin.entity.clTenant.CLTenantEntity;
import org.thingsboard.server.dft.mbgadmin.entity.tenantQL.TenantEntityQL;

import java.util.UUID;

public interface CLTenantDao {
    ResponseDataDto getPage(Pageable pageable, String typeCustomer, UUID groupService, String state, long startDate, long andDate, String textSearch);

    CLTenantUpdateResponse deleteById(UUID id, UUID currentUserId);

    CLTenantDetailDto getById(UUID id);

    CLTenantEditResponse createOrUpdate(CLTenantEditDto clTenantEditDto, UUID clUserId, UUID currentUserId);

    Boolean checkCode(String code);

    GetIdDto checkTenant(UUID clTenantId);

    AutoCodeGenerationResponse autoCodeGeneration();

    boolean isTenantUser(UUID clUserId);
}
