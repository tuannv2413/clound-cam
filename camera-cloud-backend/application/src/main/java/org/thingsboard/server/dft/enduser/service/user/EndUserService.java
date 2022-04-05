package org.thingsboard.server.dft.enduser.service.user;

import org.springframework.data.domain.Pageable;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dto.auth.ChangePasswordRequest;
import org.thingsboard.server.dft.enduser.dto.user.EndUserDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.UUID;

public interface EndUserService {

    PageData<EndUserDto> getPage(Pageable pageable, String textSearch, int active, SecurityUser securityUser) throws ThingsboardException;

    EndUserDto createOrUpdate(EndUserDto endUserDto, SecurityUser securityUser) throws ThingsboardException;

    EndUserDto getById(UUID id, SecurityUser securityUser);

    EndUserDto getByUserId(UUID id,SecurityUser securityUser);

    EndUserDto getByUserId(UUID id);

    void deleteById(UUID id,SecurityUser securityUser) throws ThingsboardException;

    String changePassword(ChangePasswordRequest request, SecurityUser securityUser) throws ThingsboardException;

    boolean existsByTenantIdAndUserId(UUID tenantId, UUID tbUserId);

}
