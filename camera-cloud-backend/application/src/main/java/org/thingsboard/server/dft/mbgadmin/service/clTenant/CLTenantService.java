package org.thingsboard.server.dft.mbgadmin.service.clTenant;

import org.springframework.http.ResponseEntity;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.dft.mbgadmin.dto.clTenant.*;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseDataDto;
import org.thingsboard.server.dft.mbgadmin.entity.clTenant.CLTenantEntity;
import org.thingsboard.server.dft.mbgadmin.entity.tenantQL.TenantEntityQL;

import java.text.ParseException;
import java.util.UUID;

public interface CLTenantService {
    ResponseDataDto getPage(int page, int pageSize, String typeCustomer, UUID groupService, String state, long startDate, long endDate, String textSearch, String sortProperty, String sortOrder) throws ParseException;

    CLTenantUpdateResponse deleteById(UUID id, UUID currentUserId);

    CLTenantDetailDto getById(UUID id);

    CLTenantEditResponse createOrUpdate(CLTenantEditDto clTenantEditDto, UUID clUserId, UUID currentUserId);

    Boolean checkCode(String code);

    GetIdDto checkTenant(UUID clUserId);

    AutoCodeGenerationResponse autoCodeGeneration();

    boolean isTenantUser(UUID clUserId);
}
