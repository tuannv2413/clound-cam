package org.thingsboard.server.dft.mbgadmin.dao.clTenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thingsboard.server.common.data.User;
import org.thingsboard.server.common.data.audit.ActionType;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.id.UserId;
import org.thingsboard.server.dao.model.sql.AuditLogEntity;
import org.thingsboard.server.dao.sql.audit.AuditLogRepository;
import org.thingsboard.server.dao.tenant.TenantService;
import org.thingsboard.server.dao.user.UserService;
import org.thingsboard.server.dft.enduser.repository.user.TBUserRepository;
import org.thingsboard.server.dft.mbgadmin.dto.CLMangeMediaBox.CLMangeMediaBoxDetailDto;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseDataDto;
import org.thingsboard.server.dft.mbgadmin.dto.ResponseMessageDto.ResponseMessageDto;
import org.thingsboard.server.dft.mbgadmin.dto.clGroupService.CLGroupServiceDto;
import org.thingsboard.server.dft.mbgadmin.dto.clServiceOption.CLServiceOptionDto;
import org.thingsboard.server.dft.mbgadmin.dto.clTenant.*;
import org.thingsboard.server.dft.mbgadmin.entity.clMangeMediaBox.CLMangeMediaBoxEntity;
import org.thingsboard.server.dft.mbgadmin.entity.clServiceOption.CLServiceOptionEntity;
import org.thingsboard.server.dft.mbgadmin.entity.clTenant.CLTenantEntity;
import org.thingsboard.server.dft.mbgadmin.entity.clUser.CLUserEntity;
import org.thingsboard.server.dft.mbgadmin.entity.tenantProfile.TenantProfileEntityQL;
import org.thingsboard.server.dft.mbgadmin.entity.tenantQL.TenantEntityQL;
import org.thingsboard.server.dft.mbgadmin.repository.clBox.CLBoxRepository;
import org.thingsboard.server.dft.mbgadmin.repository.clGroupService.CLGroupServiceRepository;
import org.thingsboard.server.dft.mbgadmin.repository.clMangeMediaBox.CLMangeMediaBoxRepository;
import org.thingsboard.server.dft.mbgadmin.repository.clServiceOption.CLServiceOptionRepository;
import org.thingsboard.server.dft.mbgadmin.repository.clTenant.CLTenantRepository;
import org.thingsboard.server.dft.mbgadmin.repository.clUser.CLUserRepository;
import org.thingsboard.server.dft.mbgadmin.repository.tenant.TenantRepositoryQL;
import org.thingsboard.server.dft.mbgadmin.repository.tenantProfile.TenantProfileRepositoryQL;
import org.thingsboard.server.dft.mbgadmin.repository.user.UserMngRepository;
import org.thingsboard.server.dft.mbgadmin.service.user.TBUserService;
import org.thingsboard.server.dft.mbgadmin.service.user.UserMngService;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class CLTenantDaoImpl implements CLTenantDao {
    private static final String activated = "1"; //kích hoạt
    private static final String notActivated = "2"; //Chưa kích họat
    private static final String lockUp = "3"; //khóa
    private static final String individual = "0"; // tư nhân
    private static final String enterprise = "1"; // doanh nghiệp

    private CLTenantRepository clTenantRepository;
    private TenantRepositoryQL tenantRepositoryQL;
    private CLUserRepository clUserRepository;
    private CLGroupServiceRepository clGroupServiceRepository;
    private CLServiceOptionRepository clServiceOptionRepository;
    private CLBoxRepository clBoxRepository;
    private CLMangeMediaBoxRepository clMangeMediaBoxRepository;
    private TenantProfileRepositoryQL tenantProfileRepositoryQL;
    private TenantService tenantService;
    protected UserService userService;
    protected UserMngService userMngService;
    private TBUserRepository userRepository;
    private UserMngRepository userMngRepository;
    private AuditLogRepository auditLogRepository;
    private TBUserService tbUserService;

    @Autowired
    public CLTenantDaoImpl(
            CLTenantRepository clTenantRepository, TenantRepositoryQL tenantRepositoryQL,
            CLUserRepository clUserRepository, CLGroupServiceRepository clGroupServiceRepository,
            CLServiceOptionRepository clServiceOptionRepository, CLBoxRepository clBoxRepository,
            CLMangeMediaBoxRepository clMangeMediaBoxRepository, TenantProfileRepositoryQL tenantProfileRepositoryQL,
            UserService userService, TenantService tenantService, UserMngService userMngService,
            TBUserRepository userRepository, UserMngRepository userMngRepository, AuditLogRepository auditLogRepository,
            TBUserService tbUserService) {
        this.clTenantRepository = clTenantRepository;
        this.tenantRepositoryQL = tenantRepositoryQL;
        this.clUserRepository = clUserRepository;
        this.clGroupServiceRepository = clGroupServiceRepository;
        this.clServiceOptionRepository = clServiceOptionRepository;
        this.clBoxRepository = clBoxRepository;
        this.clMangeMediaBoxRepository = clMangeMediaBoxRepository;
        this.tenantProfileRepositoryQL = tenantProfileRepositoryQL;
        this.tenantService = tenantService;
        this.userService = userService;
        this.userMngService = userMngService;
        this.userRepository = userRepository;
        this.userMngRepository = userMngRepository;
        this.auditLogRepository = auditLogRepository;
        this.tbUserService = tbUserService;
    }

    /**
     * Lấy danh sách bản ghi có phân trang bảng cl_tenant
     *
     * @param pageable
     * @param typeCustomer
     * @param groupService
     * @param state
     * @param startDate
     * @param andDate
     * @param textSearch
     * @return
     */
    @Override
    @Transactional
    public ResponseDataDto getPage(Pageable pageable, String typeCustomer, UUID groupService, String state, long startDate, long andDate, String textSearch) {
        ResponseDataDto result;
        List<CLTenantDto> clTenantDtos = new ArrayList<>();
        Page<CLTenantEntity> clTenantEntities = clTenantRepository.findALLAndSearch(pageable, typeCustomer, groupService, state, startDate, andDate, textSearch);
        if (clTenantEntities.getContent().size() > 0) {
            for (CLTenantEntity c : clTenantEntities) {
                long totalPrice = 0l;
                String groupServiceDB = "";
                for (CLServiceOptionEntity cls : c.getClServiceOptionEntities()) {
                    totalPrice += cls.getPrice();
                    groupServiceDB = cls.getClGroupServiceEntity().getName();
                }
                CLTenantDto clTenantDto = new CLTenantDto(c, new SimpleDateFormat("dd/MM/yyyy").format(new Date(c.getDayStartService())), groupServiceDB, totalPrice);
                clTenantDtos.add(clTenantDto);
            }
        }
        if (clTenantEntities.getSize() > 0) {
            result = ResponseDataDto.builder()
                    .data(clTenantDtos)
                    .totalPages(clTenantEntities.getTotalPages())
                    .totalElements((int) clTenantEntities.getTotalElements())
                    .hasNext(pageable.getPageNumber() == (clTenantEntities.getTotalPages() - 1) ? true : false)
                    .build();
            return result;
        } else {
            return null;
        }
    }

    /**
     * Xóa bản ghi bảng cl_tenant, cập nhật cl_tenant_id == null bản ghi bị xóa cl_mange_media_box, thay đổi trạng thái user sang false, thay đổi trường delete = true
     *
     * @param id
     */
    @Override
    @Transactional
    public CLTenantUpdateResponse deleteById(UUID id, UUID currentUserId) {
        try {
            Optional<CLTenantEntity> clTenantEntity = clTenantRepository.findById(id);
            try {
                Page<AuditLogEntity> pageAuditLogEntity = auditLogRepository.findAuditLogsByTenantIdAndUserId(id, clTenantEntity.get().getClUserEntity().getTbUserId(), "", null, null, List.of(ActionType.LOGIN), PageRequest.of(0, 1));
                if (pageAuditLogEntity != null && pageAuditLogEntity.getTotalElements() > 0) {
                    return CLTenantUpdateResponse.builder().code(51).message("Không thể xóa khách hàng này.").build();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                log.error("get audit log failed with exception: ", ex);
                return CLTenantUpdateResponse.builder().code(50).message("Xóa khách hàng thất bại. " + ex.getMessage()).build();
            }

            List<CLMangeMediaBoxEntity> clMangeMediaBoxEntities = clMangeMediaBoxRepository.findAllByClTenantEntityId(id);
            List<CLMangeMediaBoxEntity> clMangeMediaBoxEntitiesUpdate = new ArrayList<>();
            clMangeMediaBoxEntities.forEach(clMangeMediaBox -> {
                CLMangeMediaBoxEntity clMangeMediaBoxEntityUpdate = new CLMangeMediaBoxEntity();
                clMangeMediaBoxEntityUpdate.setId(clMangeMediaBox.getId());
                clMangeMediaBoxEntityUpdate.setConsignment(clMangeMediaBox.getConsignment());
                clMangeMediaBoxEntityUpdate.setFirmwareVersion(clMangeMediaBox.getFirmwareVersion());
                clMangeMediaBoxEntityUpdate.setPartNumber(clMangeMediaBox.getPartNumber());
                clMangeMediaBoxEntityUpdate.setSerialNumber(clMangeMediaBox.getSerialNumber());
                clMangeMediaBoxEntityUpdate.setStatus(clMangeMediaBox.getStatus());
                clMangeMediaBoxEntityUpdate.setType(clMangeMediaBox.getType());
                clMangeMediaBoxEntityUpdate.setClTenantEntity(null);
                clMangeMediaBoxEntityUpdate.setTenantEntityQL(clMangeMediaBox.getTenantEntityQL());
                clMangeMediaBoxEntityUpdate.setUpdatedBy(clMangeMediaBox.getUpdatedBy());
                clMangeMediaBoxEntityUpdate.setUpdatedTime(clMangeMediaBox.getUpdatedTime());
                clMangeMediaBoxEntityUpdate.setCreatedBy(clMangeMediaBox.getCreatedBy());
                clMangeMediaBoxEntityUpdate.setCreatedTime(clMangeMediaBox.getCreatedTime());
                clMangeMediaBoxEntitiesUpdate.add(clMangeMediaBoxEntityUpdate);
            });
            clMangeMediaBoxRepository.saveAll(clMangeMediaBoxEntitiesUpdate);

            // delete record cl_tenant
            clTenantRepository.deleteById(id);

            // update email record tb_user
            User tbUser = new User();
            tbUser.setId(new UserId((clTenantEntity.get().getClUserEntity().getTbUserId())));
            tbUser.setEmail(clTenantEntity.get().getClUserEntity().getTbUserId() + "-" + clTenantEntity.get().getClUserEntity().getEmail());
            tbUserService.changeUserEmail(tbUser);

            // delete record cl_user
            userMngService.deleteById(clTenantEntity.get().getClUserEntity().getTenantEntityQL().getId(), clTenantEntity.get().getClUserEntity().getId(), currentUserId);

            return CLTenantUpdateResponse.builder().code(200).message("Thành công.").build();
        } catch (Exception e) {
            e.printStackTrace();
            return CLTenantUpdateResponse.builder().code(50).message("Xóa khách hàng thất bại. " + e.getMessage()).build();
        }
    }

    /**
     * Lấy bản ghi bảng cl_tenant theo id
     *
     * @param id
     * @return
     */
    @Override
    @Transactional
    public CLTenantDetailDto getById(UUID id) {
        try {
            CLTenantDetailDto result;
            Optional<CLTenantEntity> clTenantEntity = clTenantRepository.findById(id);
            if (clTenantEntity.isEmpty()) {
                return null;
            }
            // get list MediaBoxDetailDtos
            List<CLMangeMediaBoxDetailDto> mediaBoxDetailDtos = new ArrayList<>();
            for (CLMangeMediaBoxEntity clm : clTenantEntity.get().getClMangeMediaBoxEntities()) {
                mediaBoxDetailDtos.add(new CLMangeMediaBoxDetailDto(clm));
            }

            // get CLServiceOptionDto
            List<CLServiceOptionEntity> serviceOptionEntities = new ArrayList<>();
            serviceOptionEntities.addAll(clTenantEntity.get().getClServiceOptionEntities());
            List<CLServiceOptionDto> serviceOptionEditDtos = new ArrayList<>();
            for (CLServiceOptionEntity cls : serviceOptionEntities) {
                serviceOptionEditDtos.add(new CLServiceOptionDto(cls));
            }
            result = new CLTenantDetailDto(clTenantEntity.get(), mediaBoxDetailDtos, new CLGroupServiceDto(serviceOptionEntities.get(0).getClGroupServiceEntity()), serviceOptionEditDtos, new SimpleDateFormat("dd/MM/yyyy").format(new Date(clTenantEntity.get().getDayStartService())));

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Thêm mới hoặc cập nhật bản ghi bảng cl_tenant
     *
     * @param clTenantEditDto
     * @param clUserId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public CLTenantEditResponse createOrUpdate(CLTenantEditDto clTenantEditDto, UUID clUserId, UUID currentUserId) {
        Optional<CLTenantEntity> clTenantEntityDB;

        if (clTenantEditDto.getId() != null) {
            clTenantEntityDB = clTenantRepository.findById(clTenantEditDto.getId());
        } else {
            clTenantEntityDB = null;
        }

        // get cl_user
        Optional<CLUserEntity> clUserEntityResponse = clUserRepository.findById(clUserId);

        // Insert table cl_tenant
        CLTenantEntity clTenantEntityResponse = new CLTenantEntity();
        try {
            Set<CLServiceOptionEntity> setCLServiceOptionEntities = new HashSet<CLServiceOptionEntity>();
            for (UUID id : clTenantEditDto.getServiceOptionUUID()) {
                Optional<CLServiceOptionEntity> clServiceOptionEntity = clServiceOptionRepository.findById(id);
                setCLServiceOptionEntities.add(clServiceOptionEntity.get());
            }

            long dayStartService = 0l;
            try {
                dayStartService = new SimpleDateFormat("dd/MM/yyyy").parse(clTenantEditDto.getDayStartService()).getTime();
            } catch (Exception e) {
                dayStartService = 0l;
            }
            CLTenantEntity clTenantEntity = new CLTenantEntity();
            if (clTenantEntityDB != null) {
                clTenantEntity.setId(clTenantEntityDB.get().getId());
                clTenantEntity.setUpdatedTime(new Date().getTime());
                clTenantEntity.setUpdatedBy(currentUserId);
                clTenantEntity.setCreatedTime(clTenantEntityDB.get().getCreatedTime());
                clTenantEntity.setCreatedBy(clTenantEntityDB.get().getCreatedBy());
            } else {
                clTenantEntity.setId(UUID.randomUUID());
                clTenantEntity.setCreatedTime(new Date().getTime());
                clTenantEntity.setCreatedBy(currentUserId);
            }
            clTenantEntity.setCode(clTenantEditDto.getCode());
            clTenantEntity.setAddress(clTenantEditDto.getAddress());
            clTenantEntity.setType(clTenantEditDto.getType());
            clTenantEntity.setDayStartService(dayStartService);
            clTenantEntity.setNote(clTenantEditDto.getNote());
            clTenantEntity.setClUserEntity(clUserEntityResponse.get());
            clTenantEntity.setClServiceOptionEntities(setCLServiceOptionEntities);
            clTenantEntityResponse = clTenantRepository.save(clTenantEntity);
        } catch (Exception e) {
            userMngRepository.deleteById(clUserEntityResponse.get().getId());
            userRepository.deleteById(clUserEntityResponse.get().getTbUserId());
            tenantService.deleteTenant(new TenantId(clUserEntityResponse.get().getTenantEntityQL().getId()));
//            userService.deleteUser(new TenantId(clUserEntityResponse.get().getTenantEntityQL().getId()), new UserId(clUserEntityResponse.get().getTbUserId()));
//            userMngService.deleteById(clUserEntityResponse.get().getTenantEntityQL().getId(), clUserEntityResponse.get().getTbUserId(), currentUserId);
            e.printStackTrace();
            CLTenantEditResponse result = CLTenantEditResponse.builder().code(52).data(null).build();
            return result;
        }

        // Insert table cl_mange_media_box
        List<CLMangeMediaBoxEntity> clMangeMediaBoxEntityList = new ArrayList<>();
        try {
            CLTenantEntity finalClTenantEntityResponse = clTenantEntityResponse;
            TenantEntityQL finalTenantEntityQLResponse = clUserEntityResponse.get().getTenantEntityQL();
            // Update các mediabox đã có
            if (clTenantEntityDB != null) {
                List<CLMangeMediaBoxEntity> clMangeMediaBoxEntities = clMangeMediaBoxRepository.findAllByClTenantEntityId(clTenantEntityResponse.getId());
                List<CLMangeMediaBoxEntity> clMangeMediaBoxEntitiesUpdate = new ArrayList<>();
                clMangeMediaBoxEntities.forEach(clMangeMediaBox -> {
                    CLMangeMediaBoxEntity clMangeMediaBoxEntityUpdate = new CLMangeMediaBoxEntity();
                    clMangeMediaBoxEntityUpdate.setId(clMangeMediaBox.getId());
                    clMangeMediaBoxEntityUpdate.setConsignment(clMangeMediaBox.getConsignment());
                    clMangeMediaBoxEntityUpdate.setFirmwareVersion(clMangeMediaBox.getFirmwareVersion());
                    clMangeMediaBoxEntityUpdate.setPartNumber(clMangeMediaBox.getPartNumber());
                    clMangeMediaBoxEntityUpdate.setSerialNumber(clMangeMediaBox.getSerialNumber());
                    clMangeMediaBoxEntityUpdate.setStatus(clMangeMediaBox.getStatus());
                    clMangeMediaBoxEntityUpdate.setType(clMangeMediaBox.getType());
                    clMangeMediaBoxEntityUpdate.setClTenantEntity(null);
                    clMangeMediaBoxEntityUpdate.setTenantEntityQL(null);
                    clMangeMediaBoxEntityUpdate.setUpdatedBy(clMangeMediaBox.getUpdatedBy());
                    clMangeMediaBoxEntityUpdate.setUpdatedTime(clMangeMediaBox.getUpdatedTime());
                    clMangeMediaBoxEntityUpdate.setCreatedBy(clMangeMediaBox.getCreatedBy());
                    clMangeMediaBoxEntityUpdate.setCreatedTime(clMangeMediaBox.getCreatedTime());
                    clMangeMediaBoxEntitiesUpdate.add(clMangeMediaBoxEntityUpdate);
                });
                clMangeMediaBoxRepository.saveAll(clMangeMediaBoxEntitiesUpdate);
            }
            List<CLMangeMediaBoxEntity> listNewCLMangeMediaBoxEntities = new ArrayList<>();
            clTenantEditDto.getMangeMediaBoxUuids().forEach(uuid -> {
                // check box already exist
                Optional<CLMangeMediaBoxEntity> findCLMangeMediaBox = clMangeMediaBoxRepository.findById(uuid);
                CLMangeMediaBoxEntity newClMangeMediaBoxEntity = new CLMangeMediaBoxEntity();

                if (clTenantEntityDB != null) {
                    newClMangeMediaBoxEntity.setUpdatedBy(clTenantEditDto.getCreatedOrUpdateBy());
                    newClMangeMediaBoxEntity.setUpdatedTime(new Date().getTime());
                } else {
                    newClMangeMediaBoxEntity.setCreatedTime(findCLMangeMediaBox.get().getCreatedTime());
                    newClMangeMediaBoxEntity.setCreatedBy(clTenantEditDto.getCreatedOrUpdateBy());
                }
                newClMangeMediaBoxEntity.setId(findCLMangeMediaBox.get().getId());
                newClMangeMediaBoxEntity.setClTenantEntity(finalClTenantEntityResponse);
                newClMangeMediaBoxEntity.setTenantEntityQL(finalTenantEntityQLResponse);
                newClMangeMediaBoxEntity.setType(findCLMangeMediaBox.get().getType());
                newClMangeMediaBoxEntity.setStatus(findCLMangeMediaBox.get().getStatus());
                newClMangeMediaBoxEntity.setConsignment(findCLMangeMediaBox.get().getConsignment());
                newClMangeMediaBoxEntity.setFirmwareVersion(findCLMangeMediaBox.get().getFirmwareVersion());
                newClMangeMediaBoxEntity.setSerialNumber(findCLMangeMediaBox.get().getSerialNumber());
                newClMangeMediaBoxEntity.setPartNumber(findCLMangeMediaBox.get().getPartNumber());
                listNewCLMangeMediaBoxEntities.add(newClMangeMediaBoxEntity);
            });
            clMangeMediaBoxEntityList = clMangeMediaBoxRepository.saveAll(listNewCLMangeMediaBoxEntities);
        } catch (Exception e) {
            e.printStackTrace();
            CLTenantEditResponse result = CLTenantEditResponse.builder().code(53).data(null).build();
            return result;
        }

        Optional<CLTenantEntity> clTenantEntity = clTenantRepository.findById(clTenantEntityResponse.getId());
        // get list MediaBoxDetailDtos
        List<CLMangeMediaBoxDetailDto> mediaBoxDetailDtos = new ArrayList<>();
        for (CLMangeMediaBoxEntity clm : clMangeMediaBoxEntityList) {
            mediaBoxDetailDtos.add(new CLMangeMediaBoxDetailDto(clm));
        }
        // get list CLServiceOptionEditDto
        List<CLServiceOptionEntity> serviceOptionEntities = new ArrayList<>();
        serviceOptionEntities.addAll(clTenantEntity.get().getClServiceOptionEntities());
        List<CLServiceOptionDto> serviceOptionEditDtos = new ArrayList<>();
        for (CLServiceOptionEntity cls : serviceOptionEntities) {
            serviceOptionEditDtos.add(new CLServiceOptionDto(cls));
        }
        CLTenantDetailDto clTenantDetailDto = new CLTenantDetailDto(clTenantEntity.get(), mediaBoxDetailDtos, new CLGroupServiceDto(serviceOptionEntities.get(0).getClGroupServiceEntity()), serviceOptionEditDtos, new SimpleDateFormat("dd-MM-yyyy").format(new Date(clTenantEntity.get().getDayStartService())));
        CLTenantEditResponse result = CLTenantEditResponse.builder().code(0).data(clTenantDetailDto).build();
        return result;
    }

    /**
     * Kiểm tra mã code đã tồn tại trong database
     *
     * @param code
     * @return
     */
    @Override
    @Transactional
    public Boolean checkCode(String code) {
        Boolean check = clTenantRepository.existsByCode(code);
        return check;
    }

    /**
     * Kiểm tra bản ghi bảng cl_tenant đã tồn tại theo cl_tenant_id
     *
     * @param clTenantId
     * @return
     */
    @Override
    @Transactional
    public GetIdDto checkTenant(UUID clTenantId) {
        Optional<CLTenantEntity> clTenantEntity = clTenantRepository.findById(clTenantId);
        if (clTenantEntity.isEmpty()) {
            return null;
        } else {
            GetIdDto result = GetIdDto.builder()
                    .tenantId(clTenantEntity.get().getClUserEntity().getTenantEntityQL().getId())
                    .userId(clTenantEntity.get().getClUserEntity().getId())
                    .tbUserId(clTenantEntity.get().getClUserEntity().getTbUserId())
                    .build();
            return result;
        }
    }

    @Override
    public AutoCodeGenerationResponse autoCodeGeneration() {
        long tenantCode = Long.parseLong(clTenantRepository.findCodeMax().size() > 0 ? clTenantRepository.findCodeMax().get(0).getCode() : "0") + 1l;
        String code = String.format("%09d", tenantCode);
        return AutoCodeGenerationResponse.builder().code(0).data(code).build();
    }

    @Override
    public boolean isTenantUser(UUID clUserId) {
        return clTenantRepository.isTenantUser(clUserId);
    }
}
