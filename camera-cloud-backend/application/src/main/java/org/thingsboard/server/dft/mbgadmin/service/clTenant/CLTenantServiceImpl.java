package org.thingsboard.server.dft.mbgadmin.service.clTenant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.dft.mbgadmin.dao.clTenant.CLTenantDao;
import org.thingsboard.server.dft.mbgadmin.dto.clTenant.*;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseDataDto;
import org.thingsboard.server.dft.mbgadmin.entity.clTenant.CLTenantEntity;
import org.thingsboard.server.dft.mbgadmin.entity.tenantQL.TenantEntityQL;

import java.util.UUID;

@Service
public class CLTenantServiceImpl implements CLTenantService {

    private CLTenantDao clTenantDao;

    @Autowired
    public CLTenantServiceImpl(CLTenantDao clTenantDao) {
        this.clTenantDao = clTenantDao;
    }

    @Override
    public ResponseDataDto getPage(int page, int pageSize, String typeCustomer, UUID groupService, String state, long startDate, long endDate, String textSearch, String sortProperty, String sortOrder) {
        Sort sort = null;
        if (sortOrder.equals("desc")) {
            sort = Sort.by(sortProperty).descending();
        }
        if (sortOrder.equals("asc")) {
            sort = Sort.by(sortProperty).ascending();
        }
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        return clTenantDao.getPage(pageable, typeCustomer, groupService, state, startDate, endDate, textSearch);
    }

    @Override
    public CLTenantUpdateResponse deleteById(UUID id, UUID currentUserId) {
        return clTenantDao.deleteById(id, currentUserId);
    }

    @Override
    public CLTenantDetailDto getById(UUID id) {
        return clTenantDao.getById(id);
    }

    @Override
    public CLTenantEditResponse createOrUpdate(CLTenantEditDto clTenantEditDto, UUID clUserId, UUID currentUserId) {
        return clTenantDao.createOrUpdate(clTenantEditDto, clUserId, currentUserId);
    }


    @Override
    public Boolean checkCode(String code) {
        return clTenantDao.checkCode(code);
    }

    @Override
    public GetIdDto checkTenant(UUID clUserId) {
        return clTenantDao.checkTenant(clUserId);
    }

    @Override
    public AutoCodeGenerationResponse autoCodeGeneration() {
        return clTenantDao.autoCodeGeneration();
    }

    @Override
    public boolean isTenantUser(UUID clUserId) {
        return clTenantDao.isTenantUser(clUserId);
    }
}
