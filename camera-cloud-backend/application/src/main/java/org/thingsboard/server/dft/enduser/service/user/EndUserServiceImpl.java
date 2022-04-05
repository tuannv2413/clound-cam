package org.thingsboard.server.dft.enduser.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dao.user.EndUserDao;
import org.thingsboard.server.dft.enduser.dto.auth.ChangePasswordRequest;
import org.thingsboard.server.dft.enduser.dto.user.EndUserDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EndUserServiceImpl implements EndUserService {

  private final EndUserDao endUserDao;

  @Override
  public PageData<EndUserDto> getPage(Pageable pageable, String textSearch, int active, SecurityUser securityUser)
      throws ThingsboardException {
    return endUserDao.getPage(pageable, textSearch, active, securityUser);
  }

  @Override
  @Transactional
  public EndUserDto createOrUpdate(EndUserDto endUserDto, SecurityUser securityUser) throws ThingsboardException {
    endUserDto.setCreatedTime(new Date().getTime());
    endUserDto.setUpdatedTime(new Date().getTime());
    endUserDto.setCreatedBy(securityUser.getUuidId());
    endUserDto.setUpdatedBy(securityUser.getUuidId());
    endUserDto.setTenantId(securityUser.getTenantId().getId());
    return endUserDao.createOrUpdate(endUserDto);
  }

  @Override
  public EndUserDto getById(UUID id, SecurityUser securityUser) {
    return endUserDao.getById(id, securityUser);
  }

  @Override
  public EndUserDto getByUserId(UUID id, SecurityUser securityUser) {
    return endUserDao.getByUserId(id, securityUser);
  }

  @Override
  public EndUserDto getByUserId(UUID id) {
    return endUserDao.getByUserId(id);
  }

  @Override
  public void deleteById(UUID id, SecurityUser securityUser) throws ThingsboardException {
    endUserDao.deleteById(id, securityUser);
  }

  @Override
  public String changePassword(ChangePasswordRequest request, SecurityUser securityUser) throws ThingsboardException {
    return endUserDao.changePassword(request, securityUser);
  }

  @Override
  public boolean existsByTenantIdAndUserId(UUID tenantId, UUID tbUserId) {
    return endUserDao.existsByTenantIdAndUserId(tenantId, tbUserId);
  }
}