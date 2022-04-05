package org.thingsboard.server.dft.enduser.dao.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twilio.rest.trusthub.v1.EndUser;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.thingsboard.common.util.JacksonUtil;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.common.data.security.Authority;
import org.thingsboard.server.common.data.security.UserCredentials;
import org.thingsboard.server.dao.model.sql.UserCredentialsEntity;
import org.thingsboard.server.dao.model.sql.UserEntity;
import org.thingsboard.server.dao.sql.user.UserCredentialsRepository;
import org.thingsboard.server.dao.user.UserCredentialsDao;
import org.thingsboard.server.dao.user.UserDao;
import org.thingsboard.server.dao.user.UserService;
import org.thingsboard.server.dft.enduser.dto.auth.ChangePasswordRequest;
import org.thingsboard.server.dft.enduser.dto.camera.CustomerCameraPermissionDto;
import org.thingsboard.server.dft.enduser.dto.user.EndUserDto;
import org.thingsboard.server.dft.enduser.entity.camera.CustomerCameraPermissionEntity;
import org.thingsboard.server.dft.enduser.entity.user.EndUserEntity;
import org.thingsboard.server.dft.enduser.repository.camera.CameraPermissionRepository;
import org.thingsboard.server.dft.enduser.repository.user.EndUserRepository;
import org.thingsboard.server.dft.enduser.repository.user.TBUserRepository;
import org.thingsboard.server.service.security.model.SecurityUser;
import org.thingsboard.server.service.security.system.SystemSecurityService;

import javax.transaction.Transactional;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EndUserDaoImpl implements EndUserDao {

    public static final String USER_PASSWORD_HISTORY = "userPasswordHistory";
    private static final int DEFAULT_TOKEN_LENGTH = 30;

    @Value("${security.user_login_case_sensitive:true}")
    private boolean userLoginCaseSensitive;

    private final UserDao userDao;
    private final EndUserRepository endUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserCredentialsRepository userCredentialsRepository;
    private final TBUserRepository tbUserRepository;
    protected final UserService userService;
    private final SystemSecurityService systemSecurityService;
    private final UserCredentialsDao userCredentialsDao;
    private final CameraPermissionRepository permissionRepository;

    @Autowired
    public EndUserDaoImpl(UserDao userDao, EndUserRepository endUserRepository, PasswordEncoder passwordEncoder, UserCredentialsRepository userCredentialsRepository, TBUserRepository tbUserRepository, SystemSecurityService systemSecurityService, UserService userService, UserCredentialsDao userCredentialsDao, CameraPermissionRepository permissionRepository) {
        this.userDao = userDao;
        this.endUserRepository = endUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.userCredentialsRepository = userCredentialsRepository;
        this.tbUserRepository = tbUserRepository;
        this.systemSecurityService = systemSecurityService;
        this.userService = userService;
        this.userCredentialsDao = userCredentialsDao;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public PageData<EndUserDto> getPage(Pageable pageable, String textSearch, int active, SecurityUser securityUser) throws ThingsboardException {
        Page<EndUserDto> exampleDtoPage;
        if (active == 1) {
            exampleDtoPage = endUserRepository.findAllByNameAndActive(pageable, textSearch, Boolean.TRUE, securityUser.getTenantId().getId()).map(EndUserDto::new);
        } else if (active == 2) {
            exampleDtoPage = endUserRepository.findAllByNameAndActive(pageable, textSearch, Boolean.FALSE, securityUser.getTenantId().getId()).map(EndUserDto::new);
        } else {
            exampleDtoPage = endUserRepository.findAllByName(pageable, textSearch, securityUser.getTenantId().getId()).map(EndUserDto::new);
        }
        for (int i = 0; i < exampleDtoPage.getContent().size(); i++) {
            if (exampleDtoPage.getContent().get(i).getCreatedBy() != null) {
                EndUserEntity endUserEntity = endUserRepository.findByUserId(exampleDtoPage.getContent().get(i).getCreatedBy()).orElse(null);
                if (endUserEntity != null) {
                    exampleDtoPage.getContent().get(i).setCreateName(endUserEntity.getName());
                } else {
                    UserEntity userEntity = tbUserRepository.findById(exampleDtoPage.getContent().get(i).getCreatedBy()).orElse(null);
                    if (userEntity != null) {
                        exampleDtoPage.getContent().get(i).setCreateName(userEntity.getFirstName() + "" + userEntity.getLastName());
                    }
                }
            }
            if (exampleDtoPage.getContent().get(i).getId() != null) {
                List<CustomerCameraPermissionEntity> permissionDtoList = permissionRepository.findByClUserId(exampleDtoPage.getContent().get(i).getId());
                long number = 0L;
                for (CustomerCameraPermissionEntity entity : permissionDtoList) {
                    if (Boolean.TRUE.equals(entity.isHistory()) || Boolean.TRUE.equals(entity.isLive()) || Boolean.TRUE.equals(entity.isPtz())) {
                        number = number + 1;
                        exampleDtoPage.getContent().get(i).setCameraNumber(number);
                    }
                }
            }
        }
        return new PageData<>(exampleDtoPage.getContent(), exampleDtoPage.getTotalPages(), exampleDtoPage.getTotalElements(), exampleDtoPage.hasNext());
    }

    private String validateAndGetPhone(String phone) throws ThingsboardException {
        if (phone == null || StringUtils.isBlank(phone)) {
            throw new ThingsboardException("Số điện thoại sai định dạng!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        } else {
            if (phone.startsWith("+")) {
                phone = "0" + phone.substring(3).trim();
            }
            Pattern pattern = Pattern.compile("^\\d{10}$");
            Matcher m = pattern.matcher(phone);
            if (Boolean.FALSE.equals(m.matches())) {
                throw new ThingsboardException("Số điện thoại sai định dạng!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
            }
            return phone;
        }
    }

    @Transactional
    @Override
    public EndUserDto createOrUpdate(EndUserDto endUserDto) throws ThingsboardException {
        UserEntity userEntity = new UserEntity();
        EndUserEntity endUserEntity = null;
        UserCredentialsEntity userCredentialsEntity = null;
        String phone = null;
        if (endUserDto.getPhone() != null) {
            phone = validateAndGetPhone(endUserDto.getPhone());
        }
        if ((endUserDto.getEmail().trim().length() > 0) || (endUserDto.getEmail() != null && endUserDto.getEmail().trim().length() > 0)) {
            EndUserEntity existEndUserEntity;
            if (endUserDto.getEmail() != null) {
                EndUserEntity endUserEntity1 = endUserRepository.findByEmailAndPhone(endUserDto.getEmail(), phone);
                if (endUserEntity1 != null) {
                    endUserEntity = endUserEntity1;
                    endUserEntity.setUpdatedTime(endUserEntity1.getUpdatedTime());
                    endUserEntity.setUpdatedBy(endUserEntity1.getUpdatedBy());
                }
            }
            if (endUserEntity == null) {
                endUserEntity = new EndUserEntity();
                if (endUserDto.getEmail() != null && endUserDto.getEmail().trim().length() > 0) {
                    existEndUserEntity = endUserRepository.findByEmail(endUserDto.getEmail().trim());
//                    existEndUserEntity = endUserRepository.findByEmailIgnoreCaseAndDeleteTrue(endUserDto.getEmail());
                    if (endUserDto.getId() != null) {
                        if (existEndUserEntity != null && !existEndUserEntity.getId().equals(endUserDto.getId())) {
                            throw new ThingsboardException("Địa chỉ email đã tồn tại!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                        }
                    } else {
                        if (existEndUserEntity != null) {
                            throw new ThingsboardException("Địa chỉ email đã tồn tại!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                        }
                    }
                }

                if (endUserDto.getType() != Authority.CUSTOMER_USER && endUserDto.getType() != Authority.TENANT_ADMIN) {
                    throw new ThingsboardException("Param is not Customer_User", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                }
                if (endUserDto.getName() == null) {
                    throw new ThingsboardException("Param name is null!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                }
                if (phone.length() > 0) {
                    existEndUserEntity = endUserRepository.findByPhone(phone);
                    if (endUserDto.getId() != null) {
                        if (existEndUserEntity != null && !existEndUserEntity.getId().equals(endUserDto.getId())) {
                            throw new ThingsboardException("Số điện thoại đã tồn tại!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                        }
                    } else {
                        if (existEndUserEntity != null) {
                            throw new ThingsboardException("Số điện thoại đã tồn tại!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                        }
                    }
                }

                if (endUserDto.getId() != null) {
                    Optional<EndUserEntity> endUser = endUserRepository.findById(endUserDto.getId());
                    Optional<UserEntity> user = tbUserRepository.findById(endUserDto.getTbUserId());
                    if (user.isPresent()) {
                        userEntity = user.get();
                    } else {
                        userEntity.setId(endUserEntity.getUserId());
                        userEntity.setCreatedTime(endUserDto.getCreatedTime());
                    }
                    if (endUser.isPresent()) {
                        endUserEntity = endUser.get();
                        endUserEntity.setUpdatedTime(endUserDto.getUpdatedTime());
                        endUserEntity.setUpdatedBy(endUserDto.getUpdatedBy());
                    } else {
                        endUserEntity.setUserId(endUserDto.getTbUserId());
                        endUserEntity.setCreatedBy(endUserDto.getCreatedBy());
                        endUserEntity.setCreatedTime(endUserDto.getCreatedTime());
                    }
                } else {
                    userEntity.setId(UUID.randomUUID());
                    userEntity.setCreatedTime(endUserDto.getCreatedTime());

                    endUserEntity.setId(UUID.randomUUID());
                    endUserEntity.setCreatedBy(endUserDto.getCreatedBy());
                    endUserEntity.setCreatedTime(endUserDto.getCreatedTime());
                    endUserEntity.setUserId(userEntity.getUuid());

                    userCredentialsEntity = new UserCredentialsEntity();
                    userCredentialsEntity.setUuid(UUID.randomUUID());
                    userCredentialsEntity.setCreatedTime(endUserDto.getCreatedTime());
                    userCredentialsEntity.setEnabled(Boolean.TRUE);
                    userCredentialsEntity.setUserId(userEntity.getUuid());
                    if (endUserDto.getPassword() != null && endUserDto.getPassword().trim().length() > 0) {
                        userCredentialsEntity.setPassword(passwordEncoder.encode(endUserDto.getPassword()));
                    } else {
                        throw new ThingsboardException("Password is null", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                    }
                }
            }


            //
            UserEntity existedUserEntityByEmail = tbUserRepository.findByEmailIgnoreCase(endUserDto.getEmail());
            if (existedUserEntityByEmail != null) {
                userEntity = existedUserEntityByEmail;
                endUserEntity.setUserId(userEntity.getId());
            }

            EndUserEntity existedEndUserEntityByPhoneOrEmail = null;
            if (endUserDto.getEmail() != null) {
                existedEndUserEntityByPhoneOrEmail = endUserRepository.findByEmailIgnoreCaseAndDeleteTrue(endUserDto.getEmail());
            }
            if (endUserDto.getEmail() == null) {
                existedEndUserEntityByPhoneOrEmail = endUserRepository.findByPhoneAndDeleteTrue(phone);
            }
            if (endUserDto.getEmail() != null) {
                existedEndUserEntityByPhoneOrEmail = endUserRepository.findByEmailIgnoreCaseAndPhoneAndDeleteTrue(endUserDto.getEmail(), phone);
            }
            if (existedEndUserEntityByPhoneOrEmail != null) {
                endUserEntity = existedEndUserEntityByPhoneOrEmail;
            }


            userEntity.setCustomerId(endUserDto.getCustomerId());
            userEntity.setEmail(endUserDto.getEmail());
            userEntity.setSearchText(endUserDto.getSearchText());
            userEntity.setTenantId(endUserDto.getTenantId());
            userEntity.setAuthority(endUserDto.getType());

            userEntity = tbUserRepository.save(userEntity);

            endUserEntity.setName(endUserDto.getName());
            endUserEntity.setActive(endUserDto.isActive());
            endUserEntity.setPhone(phone);
            endUserEntity.setTenantId(endUserDto.getTenantId());
            endUserEntity.setDelete(Boolean.FALSE);
            endUserEntity.setEmail(endUserDto.getEmail());
            endUserEntity.setType(endUserDto.getType());

            endUserEntity = endUserRepository.save(endUserEntity);

            if (userCredentialsEntity != null) {
                userCredentialsRepository.save(userCredentialsEntity);
            }
            return new EndUserDto(endUserEntity, userEntity);
        } else {
            throw new ThingsboardException("Param is null", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }

    }

    @Override
    public EndUserDto getById(UUID id, SecurityUser securityUser) {
        EndUserEntity endUser;
        Optional<EndUserEntity> endUserEntity = endUserRepository.findByIdAndTenantId(id, securityUser.getTenantId().getId());
        if (endUserEntity.isPresent() && Boolean.FALSE.equals(endUserEntity.get().isDelete())) {
            endUser = endUserEntity.get();
            return new EndUserDto(endUser);
        } else {
            return null;
        }
    }

    @Override
    public EndUserDto getByUserId(UUID id, SecurityUser securityUser) {
        Optional<UserEntity> userEntity = tbUserRepository.findByIdAndTenantId(id, securityUser.getTenantId().getId());
        return getEndUserDto(id, userEntity);
    }

    @Override
    public EndUserDto getByUserId(UUID id) {
        Optional<UserEntity> userEntity = tbUserRepository.findById(id);
        return getEndUserDto(id, userEntity);
    }

    @Nullable
    private EndUserDto getEndUserDto(UUID id, Optional<UserEntity> userEntity) {
        if (userEntity.isPresent()) {
            Optional<EndUserEntity> endUserEntity = endUserRepository.findByUserId(id);
            if (endUserEntity.isPresent() && Boolean.FALSE.equals(endUserEntity.get().isDelete())) {
                return new EndUserDto(endUserEntity.get(), userEntity.get());
            } else {
                EndUserDto endUserDto = new EndUserDto();
                endUserDto.setTbUserId(userEntity.get().getId());
                endUserDto.setEmail(userEntity.get().getEmail());
                endUserDto.setName(userEntity.get().getFirstName() + " " + userEntity.get().getLastName());
                endUserDto.setTenantId(userEntity.get().getTenantId());
                endUserDto.setType(userEntity.get().getAuthority());
                endUserDto.setCreatedTime(userEntity.get().getCreatedTime());
                return endUserDto;
            }
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    public void deleteById(UUID id, SecurityUser securityUser) throws ThingsboardException {
        EndUserEntity endUserEntity = endUserRepository.findByIdAndTenantId(id, securityUser.getTenantId().getId()).orElse(null);
        if (endUserEntity != null) {
            endUserEntity.setDelete(Boolean.TRUE);
            endUserEntity.setUpdatedTime(new Date().getTime());
            endUserEntity.setUpdatedBy(securityUser.getUuidId());
            endUserRepository.save(endUserEntity);
            UserCredentials userCredentials = userCredentialsDao.findByUserId(new TenantId(endUserEntity.getTenantId()), endUserEntity.getUserId());
            if (userCredentials != null) {
                userCredentials.setEnabled(Boolean.FALSE);
                userCredentialsDao.save(userCredentials);
            }
        }
    }

    @Override
    public EndUserEntity findByPhone(TenantId tenantId, String phone) {
        return endUserRepository.findByPhoneAndDelete(phone, false);
    }

    @Override
    @Transactional
    public String changePassword(ChangePasswordRequest request, SecurityUser securityUser) throws ThingsboardException {
        UserCredentials userCredentials = userService.findUserCredentialsByUserId(TenantId.SYS_TENANT_ID, securityUser.getId());
        if (!passwordEncoder.matches(request.getOldPass(), userCredentials.getPassword())) {
            throw new ThingsboardException("Mật khẩu cũ không đúng!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
        }
        systemSecurityService.validatePassword(securityUser.getTenantId(), request.getNewPass(), userCredentials);
        if (passwordEncoder.matches(request.getNewPass(), userCredentials.getPassword())) {
            throw new ThingsboardException("New password should be different from existing!", ThingsboardErrorCode.NEWPASSWORD_SAME_OLDPASSWROD);
        }
        userCredentials.setPassword(passwordEncoder.encode(request.getNewPass()));
        replaceUserCredentials(securityUser.getTenantId(), userCredentials);
        return null;
    }

    @Override
    public boolean existsByTenantIdAndUserId(UUID tenantId, UUID tbUserId) {
        return endUserRepository.existsByTenantIdAndUserId(tenantId, tbUserId);
    }

    public UserCredentials replaceUserCredentials(TenantId tenantId, UserCredentials userCredentials) {
        //log.trace("Executing replaceUserCredentials [{}]", userCredentials);
        //userCredentialsValidator.validate(userCredentials, data -> tenantId);
        userCredentialsDao.removeById(tenantId, userCredentials.getUuidId());
        userCredentials.setId(null);
        return saveUserCredentialsAndPasswordHistory(tenantId, userCredentials);
    }

    private UserCredentials saveUserCredentialsAndPasswordHistory(TenantId tenantId, UserCredentials userCredentials) {
        UserCredentials result = userCredentialsDao.save(tenantId, userCredentials);
        User user = userService.findUserById(tenantId, userCredentials.getUserId());
        if (userCredentials.getPassword() != null) {
            updatePasswordHistory(user, userCredentials);
        }
        return result;
    }

    private void updatePasswordHistory(User user, UserCredentials userCredentials) {
        JsonNode additionalInfo = user.getAdditionalInfo();
        if (!(additionalInfo instanceof ObjectNode)) {
            additionalInfo = JacksonUtil.newObjectNode();
        }
        Map<String, String> userPasswordHistoryMap = null;
        JsonNode userPasswordHistoryJson;
        if (additionalInfo.has(USER_PASSWORD_HISTORY)) {
            userPasswordHistoryJson = additionalInfo.get(USER_PASSWORD_HISTORY);
            userPasswordHistoryMap = JacksonUtil.convertValue(userPasswordHistoryJson, new TypeReference<>() {
            });
        }
        if (userPasswordHistoryMap != null) {
            userPasswordHistoryMap.put(Long.toString(System.currentTimeMillis()), userCredentials.getPassword());
            userPasswordHistoryJson = JacksonUtil.valueToTree(userPasswordHistoryMap);
            ((ObjectNode) additionalInfo).replace(USER_PASSWORD_HISTORY, userPasswordHistoryJson);
        } else {
            userPasswordHistoryMap = new HashMap<>();
            userPasswordHistoryMap.put(Long.toString(System.currentTimeMillis()), userCredentials.getPassword());
            userPasswordHistoryJson = JacksonUtil.valueToTree(userPasswordHistoryMap);
            ((ObjectNode) additionalInfo).set(USER_PASSWORD_HISTORY, userPasswordHistoryJson);
        }
        user.setAdditionalInfo(additionalInfo);
        saveUser(user);
    }

    public User saveUser(User user) {
        //log.trace("Executing saveUser [{}]", user);
        //userValidator.validate(user, User::getTenantId);
        if (!userLoginCaseSensitive) {
            user.setEmail(user.getEmail().toLowerCase());
        }
        User savedUser = userDao.save(user.getTenantId(), user);
        if (user.getId() == null) {
            UserCredentials userCredentials = new UserCredentials();
            userCredentials.setEnabled(false);
            userCredentials.setActivateToken(RandomStringUtils.randomAlphanumeric(DEFAULT_TOKEN_LENGTH));
            userCredentials.setUserId(new UserId(savedUser.getUuidId()));
            saveUserCredentialsAndPasswordHistory(user.getTenantId(), userCredentials);
        }
        return savedUser;
    }
}
