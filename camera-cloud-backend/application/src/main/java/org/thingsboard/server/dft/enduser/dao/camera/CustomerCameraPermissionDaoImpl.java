package org.thingsboard.server.dft.enduser.dao.camera;

import org.springframework.stereotype.Component;
import org.thingsboard.server.common.data.exception.ThingsboardErrorCode;
import org.thingsboard.server.common.data.exception.ThingsboardException;
import org.thingsboard.server.dft.enduser.dto.camera.CustomerCameraPermissionDto;
import org.thingsboard.server.dft.enduser.dto.camera.CustomerCameraPermissionSaveDto;
import org.thingsboard.server.dft.enduser.entity.camera.CameraEntity;
import org.thingsboard.server.dft.enduser.entity.camera.CustomerCameraPermissionEntity;
import org.thingsboard.server.dft.enduser.entity.user.EndUserEntity;
import org.thingsboard.server.dft.enduser.repository.camera.CameraCustomRepository;
import org.thingsboard.server.dft.enduser.repository.camera.CameraPermissionRepository;
import org.thingsboard.server.dft.enduser.repository.camera.CameraRepository;
import org.thingsboard.server.dft.enduser.repository.user.EndUserRepository;
import org.thingsboard.server.service.security.model.SecurityUser;

import javax.transaction.Transactional;
import java.util.*;

@Component
public class CustomerCameraPermissionDaoImpl implements CustomerCameraPermissionDao {

    private final EndUserRepository userRepository;
    private final CameraRepository cameraRepository;
    private final CameraPermissionRepository permissionRepository;
    private final CameraCustomRepository cameraCustomRepository;

    public CustomerCameraPermissionDaoImpl(EndUserRepository userRepository, CameraRepository cameraRepository, CameraPermissionRepository permissionRepository, CameraCustomRepository cameraCustomRepository) {
        this.userRepository = userRepository;
        this.cameraRepository = cameraRepository;
        this.permissionRepository = permissionRepository;
        this.cameraCustomRepository = cameraCustomRepository;
    }

    @Override
    public List<CustomerCameraPermissionDto> getAllCustomerCameraPermissionV1(UUID boxId, UUID groupId, UUID clUserId, String cameraName, int noneGroup, UUID tenantId) throws ThingsboardException {
        List<CustomerCameraPermissionDto> response = new ArrayList<>();
        List<CameraEntity> cameraEntityList = cameraCustomRepository.findBy(boxId, groupId, cameraName, tenantId);

        for (CameraEntity cameraEntity : cameraEntityList) {
            if (Boolean.FALSE.equals(cameraEntity.isDelete())) {
                EndUserEntity endUserEntity = userRepository.findByUserId(clUserId).orElse(null);
                UUID id = null;
                if (clUserId != null) {
                    if (endUserEntity != null) {
                        id = endUserEntity.getId();
                    } else {
                        EndUserEntity endUserEntity1 = userRepository.findById(clUserId).orElse(null);
                        if (endUserEntity1 != null) {
                            id = endUserEntity1.getId();
                        }
                    }
                    if (id == null) {
                        throw new ThingsboardException("User id not found!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                    }

                    CustomerCameraPermissionEntity permission = permissionRepository.findByClCameraIdAndAndClUserId(cameraEntity.getId(), clUserId);
                    if (permission != null) {
                        response.add(CustomerCameraPermissionDto.builder()
                                .cameraName(cameraEntity.getCameraName())
                                .clCameraId(cameraEntity.getId())
                                .userID(permission.getClUserId())
                                .live(permission.isLive())
                                .ptz(permission.isPtz())
                                .history(permission.isHistory())
                                .build());
                    } else {
                        response.add(CustomerCameraPermissionDto.builder()
                                .cameraName(cameraEntity.getCameraName())
                                .clCameraId(cameraEntity.getId())
                                .userID(clUserId)
                                .live(false)
                                .ptz(false)
                                .history(false)
                                .build());
                    }
                } else {
                    response.add(CustomerCameraPermissionDto.builder()
                            .cameraName(cameraEntity.getCameraName())
                            .clCameraId(cameraEntity.getId())
                            .userID(null)
                            .live(false)
                            .ptz(false)
                            .history(false)
                            .build());
                }
            }
        }
        List<CustomerCameraPermissionDto> response1 = new ArrayList<>();

        for (CustomerCameraPermissionDto cameraPermissionDto : response) {
            Optional<CameraEntity> cameraEntity = cameraRepository.findById(cameraPermissionDto.getClCameraId());
            if (noneGroup == 1) {
                response1.add(cameraPermissionDto);
            } else {
                if (cameraEntity.isPresent() && cameraEntity.get().getCameraGroupId() == null) {
                    response1.add(cameraPermissionDto);
                }
            }
        }
        Comparator<CustomerCameraPermissionDto> comparator = Comparator
                .comparing(CustomerCameraPermissionDto::getLive)
                .thenComparing(CustomerCameraPermissionDto::getPtz)
                .thenComparing((o1, o2) -> o2.getCameraName().compareToIgnoreCase(o1.getCameraName())).reversed();
        response1.sort(comparator);
        return response1;
    }

    @Transactional
    @Override
    public List<CustomerCameraPermissionDto> saveCustomerCameraPermission(CustomerCameraPermissionSaveDto request, UUID userId) throws ThingsboardException {
        UUID id = null;
        CustomerCameraPermissionEntity permission;
        UUID tenantId = null;
        for (CustomerCameraPermissionDto dto : request.getPermissionList()) {
            permission = permissionRepository.findByClCameraIdAndAndClUserId(dto.getClCameraId(), dto.getUserID());
            if (permission != null) {
                id = dto.getUserID();
                permission.setPtz(dto.getPtz());
                permission.setLive(dto.getLive());
                permission.setHistory(dto.getHistory());
                CameraEntity cameraEntity = cameraRepository.findById(dto.getClCameraId()).orElse(null);
                if (cameraEntity != null) {
                    tenantId = cameraEntity.getTenantId();
                }
            } else {
                permission = new CustomerCameraPermissionEntity();
                CameraEntity cameraEntity = cameraRepository.findById(dto.getClCameraId()).orElse(null);
                permission.setId(UUID.randomUUID());
                if (cameraEntity != null) {
                    permission.setClCameraId(dto.getClCameraId());
                    tenantId = cameraEntity.getTenantId();
                } else {
                    throw new ThingsboardException("Camera id not found!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                }
                EndUserEntity userEntity = userRepository.findById(dto.getUserID()).orElse(null);
                if (userEntity != null) {
                    id = dto.getUserID();
                    permission.setClUserId(userEntity.getId());
                } else {
                    throw new ThingsboardException("User id not found!", ThingsboardErrorCode.BAD_REQUEST_PARAMS);
                }
                permission.setLive(dto.getLive());
                permission.setHistory(dto.getHistory());
                permission.setPtz(dto.getPtz());
            }
            permissionRepository.save(permission);
        }
        return getAllCustomerCameraPermissionV1(null, null, id, null, 1, tenantId);
    }

    @Override
    public boolean checkPtzPermission(SecurityUser securityUser, UUID cameraId) {
        Optional<EndUserEntity> optionalCLUserEntity = userRepository.findByUserId(securityUser.getUuidId());
        return optionalCLUserEntity.filter(endUserEntity -> permissionRepository
                .existsByClUserIdAndClCameraIdAndPtzTrue(endUserEntity.getId(), cameraId)).isPresent();
    }

    @Override
    public boolean checkHistoryPermission(SecurityUser securityUser, UUID cameraId) {
        Optional<EndUserEntity> optionalCLUserEntity = userRepository.findByUserId(securityUser.getUuidId());
        return optionalCLUserEntity.filter(endUserEntity -> permissionRepository
                .existsByClUserIdAndClCameraIdAndHistoryTrue(endUserEntity.getId(), cameraId)).isPresent();
    }

    @Override
    public boolean checkViewLivePermission(SecurityUser securityUser, UUID cameraId) {
        Optional<EndUserEntity> optionalCLUserEntity = userRepository.findByUserId(securityUser.getUuidId());
        return optionalCLUserEntity.filter(endUserEntity -> permissionRepository
                .existsByClUserIdAndClCameraIdAndLiveTrue(endUserEntity.getId(), cameraId)).isPresent();
    }


}
