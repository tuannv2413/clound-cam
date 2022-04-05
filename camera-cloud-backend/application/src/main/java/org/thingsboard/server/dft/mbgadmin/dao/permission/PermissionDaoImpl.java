package org.thingsboard.server.dft.mbgadmin.dao.permission;

import org.springframework.stereotype.Component;
import org.thingsboard.server.dft.mbgadmin.dto.permission.PermissionDto;
import org.thingsboard.server.dft.mbgadmin.dto.permission.PermissionGetChildDto;
import org.thingsboard.server.dft.mbgadmin.dto.permission.PermissionGetDto;
import org.thingsboard.server.dft.mbgadmin.entity.permission.PermissionEntity;
import org.thingsboard.server.dft.mbgadmin.repository.permission.PermissionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PermissionDaoImpl implements PermissionDao  {
    private final PermissionRepository permissionRepository;

    public PermissionDaoImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<PermissionGetDto> getAll() {
        List<PermissionEntity> permissionEntityList  = permissionRepository.findAll();
        if(permissionEntityList.size() == 0){
            PermissionEntity permissionQLNDALL = new PermissionEntity();
            permissionQLNDALL.setId(UUID.randomUUID());
            permissionQLNDALL.setPermission("QLND_ALL");
            permissionRepository.save(permissionQLNDALL);

            PermissionEntity permissionQLNDVIEW = new PermissionEntity();
            permissionQLNDVIEW.setId(UUID.randomUUID());
            permissionQLNDVIEW.setPermission("QLND_VIEW");
            permissionRepository.save(permissionQLNDVIEW);

            PermissionEntity permissionQLNDCREATE = new PermissionEntity();
            permissionQLNDCREATE.setId(UUID.randomUUID());
            permissionQLNDCREATE.setPermission("QLND_CREATE");
            permissionRepository.save(permissionQLNDCREATE);

            PermissionEntity permissionQLNDUPDATE = new PermissionEntity();
            permissionQLNDUPDATE.setId(UUID.randomUUID());
            permissionQLNDUPDATE.setPermission("QLND_UPDATE");
            permissionRepository.save(permissionQLNDUPDATE);

            PermissionEntity permissionQLNDDELETE = new PermissionEntity();
            permissionQLNDDELETE.setId(UUID.randomUUID());
            permissionQLNDDELETE.setPermission("QLND_DELETE");
            permissionRepository.save(permissionQLNDDELETE);

            PermissionEntity permissionQLNNDALL = new PermissionEntity();
            permissionQLNNDALL.setId(UUID.randomUUID());
            permissionQLNNDALL.setPermission("QLNND_ALL");
            permissionRepository.save(permissionQLNNDALL);

            PermissionEntity permissionQLNNDVIEW = new PermissionEntity();
            permissionQLNNDVIEW.setId(UUID.randomUUID());
            permissionQLNNDVIEW.setPermission("QLNND_VIEW");
            permissionRepository.save(permissionQLNNDVIEW);

            PermissionEntity permissionQLNNDCREATE = new PermissionEntity();
            permissionQLNNDCREATE.setId(UUID.randomUUID());
            permissionQLNNDCREATE.setPermission("QLNND_CREATE");
            permissionRepository.save(permissionQLNNDCREATE);

            PermissionEntity permissionQLNNDUPDATE = new PermissionEntity();
            permissionQLNNDUPDATE.setId(UUID.randomUUID());
            permissionQLNNDUPDATE.setPermission("QLNND_UPDATE");
            permissionRepository.save(permissionQLNNDUPDATE);

            PermissionEntity permissionQLNNDDELETE = new PermissionEntity();
            permissionQLNNDDELETE.setId(UUID.randomUUID());
            permissionQLNNDDELETE.setPermission("QLNND_DELETE");
            permissionRepository.save(permissionQLNNDDELETE);

            PermissionEntity permissionLSNDALL = new PermissionEntity();
            permissionLSNDALL.setId(UUID.randomUUID());
            permissionLSNDALL.setPermission("LSND_ALL");
            permissionRepository.save(permissionLSNDALL);

            PermissionEntity permissionLSNDVIEW = new PermissionEntity();
            permissionLSNDVIEW.setId(UUID.randomUUID());
            permissionLSNDVIEW.setPermission("LSND_VIEW");
            permissionRepository.save(permissionLSNDVIEW);

            PermissionEntity permissionQLKHALL = new PermissionEntity();
            permissionQLKHALL.setId(UUID.randomUUID());
            permissionQLKHALL.setPermission("QLKH_ALL");
            permissionRepository.save(permissionQLKHALL);

            PermissionEntity permissionQLKHVIEW = new PermissionEntity();
            permissionQLKHVIEW.setId(UUID.randomUUID());
            permissionQLKHVIEW.setPermission("QLKH_VIEW");
            permissionRepository.save(permissionQLKHVIEW);

            PermissionEntity permissionQLKHCREATE = new PermissionEntity();
            permissionQLKHCREATE.setId(UUID.randomUUID());
            permissionQLKHCREATE.setPermission("QLKH_CREATE");
            permissionRepository.save(permissionQLKHCREATE);

            PermissionEntity permissionQLKHUPDATE = new PermissionEntity();
            permissionQLKHUPDATE.setId(UUID.randomUUID());
            permissionQLKHUPDATE.setPermission("QLKH_UPDATE");
            permissionRepository.save(permissionQLKHUPDATE);

            PermissionEntity permissionQLKHDELETE = new PermissionEntity();
            permissionQLKHDELETE.setId(UUID.randomUUID());
            permissionQLKHDELETE.setPermission("QLKH_DELETE");
            permissionRepository.save(permissionQLKHDELETE);

            PermissionEntity permissionQLNDVALL = new PermissionEntity();
            permissionQLNDVALL.setId(UUID.randomUUID());
            permissionQLNDVALL.setPermission("QLNDV_ALL");
            permissionRepository.save(permissionQLNDVALL);

            PermissionEntity permissionQLNDVVIEW = new PermissionEntity();
            permissionQLNDVVIEW.setId(UUID.randomUUID());
            permissionQLNDVVIEW.setPermission("QLNDV_VIEW");
            permissionRepository.save(permissionQLNDVVIEW);

            PermissionEntity permissionQLNDVCREATE = new PermissionEntity();
            permissionQLNDVCREATE.setId(UUID.randomUUID());
            permissionQLNDVCREATE.setPermission("QLNDV_CREATE");
            permissionRepository.save(permissionQLNDVCREATE);

            PermissionEntity permissionQLNDVUPDATE = new PermissionEntity();
            permissionQLNDVUPDATE.setId(UUID.randomUUID());
            permissionQLNDVUPDATE.setPermission("QLNDV_UPDATE");
            permissionRepository.save(permissionQLNDVUPDATE);

            PermissionEntity permissionQLNDVDELETE = new PermissionEntity();
            permissionQLNDVDELETE.setId(UUID.randomUUID());
            permissionQLNDVDELETE.setPermission("QLNDV_DELETE");
            permissionRepository.save(permissionQLNDVDELETE);

            PermissionEntity permissionQLBALL = new PermissionEntity();
            permissionQLBALL.setId(UUID.randomUUID());
            permissionQLBALL.setPermission("QLB_ALL");
            permissionRepository.save(permissionQLBALL);

            PermissionEntity permissionQLBVIEW = new PermissionEntity();
            permissionQLBVIEW.setId(UUID.randomUUID());
            permissionQLBVIEW.setPermission("QLB_VIEW");
            permissionRepository.save(permissionQLBVIEW);

            PermissionEntity permissionQLBCREATE = new PermissionEntity();
            permissionQLBCREATE.setId(UUID.randomUUID());
            permissionQLBCREATE.setPermission("QLB_CREATE");
            permissionRepository.save(permissionQLBCREATE);

            PermissionEntity permissionQLBUPDATE = new PermissionEntity();
            permissionQLBUPDATE.setId(UUID.randomUUID());
            permissionQLBUPDATE.setPermission("QLB_UPDATE");
            permissionRepository.save(permissionQLBUPDATE);

            PermissionEntity permissionQLBDELETE = new PermissionEntity();
            permissionQLBDELETE.setId(UUID.randomUUID());
            permissionQLBDELETE.setPermission("QLB_DELETE");
            permissionRepository.save(permissionQLBDELETE);

            PermissionEntity permissionQLCHALL = new PermissionEntity();
            permissionQLCHALL.setId(UUID.randomUUID());
            permissionQLCHALL.setPermission("QLCH_ALL");
            permissionRepository.save(permissionQLCHALL);

            PermissionEntity permissionQLCHVIEW = new PermissionEntity();
            permissionQLCHVIEW.setId(UUID.randomUUID());
            permissionQLCHVIEW.setPermission("QLCH_VIEW");
            permissionRepository.save(permissionQLCHVIEW);

            PermissionEntity permissionQLCHCREATE = new PermissionEntity();
            permissionQLCHCREATE.setId(UUID.randomUUID());
            permissionQLCHCREATE.setPermission("QLCH_CREATE");
            permissionRepository.save(permissionQLCHCREATE);

            PermissionEntity permissionQLCHUPDATE = new PermissionEntity();
            permissionQLCHUPDATE.setId(UUID.randomUUID());
            permissionQLCHUPDATE.setPermission("QLCH_UPDATE");
            permissionRepository.save(permissionQLCHUPDATE);

            PermissionEntity permissionQLCHDELETE = new PermissionEntity();
            permissionQLCHDELETE.setId(UUID.randomUUID());
            permissionQLCHDELETE.setPermission("QLCH_DELETE");
            permissionRepository.save(permissionQLCHDELETE);

            PermissionEntity permissionBCKNBALL = new PermissionEntity();
            permissionBCKNBALL.setId(UUID.randomUUID());
            permissionBCKNBALL.setPermission("BCKNB_ALL");
            permissionRepository.save(permissionBCKNBALL);

            PermissionEntity permissionBCKNBVIEW = new PermissionEntity();
            permissionBCKNBVIEW.setId(UUID.randomUUID());
            permissionBCKNBVIEW.setPermission("BCKNB_VIEW");
            permissionRepository.save(permissionBCKNBVIEW);

            PermissionEntity permissionBCKNCALL = new PermissionEntity();
            permissionBCKNCALL.setId(UUID.randomUUID());
            permissionBCKNCALL.setPermission("BCKNC_ALL");
            permissionRepository.save(permissionBCKNCALL);

            PermissionEntity permissionBCKNCVIEW = new PermissionEntity();
            permissionBCKNCVIEW.setId(UUID.randomUUID());
            permissionBCKNCVIEW.setPermission("BCKNC_VIEW");
            permissionRepository.save(permissionBCKNCVIEW);

            PermissionEntity permissionBCTTNDALL = new PermissionEntity();
            permissionBCTTNDALL.setId(UUID.randomUUID());
            permissionBCTTNDALL.setPermission("BCTTND_ALL");
            permissionRepository.save(permissionBCTTNDALL);

            PermissionEntity permissionBCTTNDVIEW = new PermissionEntity();
            permissionBCTTNDVIEW.setId(UUID.randomUUID());
            permissionBCTTNDVIEW.setPermission("BCTTND_VIEW");
            permissionRepository.save(permissionBCTTNDVIEW);
        }

        permissionEntityList  = permissionRepository.findAll();
        PermissionGetDto permissionGetQTHT = new PermissionGetDto();
        PermissionGetDto permissionGetQTTT = new PermissionGetDto();
        PermissionGetDto permissionGetBC = new PermissionGetDto();

        List<PermissionGetChildDto> permissionQTHT = new ArrayList<>();
        List<PermissionGetChildDto> permissionQTTT = new ArrayList<>();
        List<PermissionGetChildDto> permissionBC= new ArrayList<>();

        PermissionGetChildDto permissionGetChildQLND = new PermissionGetChildDto();
        PermissionGetChildDto permissionGetChildQLNND = new PermissionGetChildDto();
        PermissionGetChildDto permissionGetChildLSND = new PermissionGetChildDto();
        PermissionGetChildDto permissionGetChildQLKH = new PermissionGetChildDto();
        PermissionGetChildDto permissionGetChildQLNDV = new PermissionGetChildDto();
        PermissionGetChildDto permissionGetChildQLB = new PermissionGetChildDto();
        PermissionGetChildDto permissionGetChildQLCH = new PermissionGetChildDto();
        PermissionGetChildDto permissionGetChildBCKNB = new PermissionGetChildDto();
        PermissionGetChildDto permissionGetChildBCKNC = new PermissionGetChildDto();
        PermissionGetChildDto permissionGetChildBCTTND = new PermissionGetChildDto();

        List<PermissionDto> permissionQLND = new ArrayList<>();
        List<PermissionDto> permissionQLNND = new ArrayList<>();
        List<PermissionDto> permissionLSND = new ArrayList<>();
        List<PermissionDto> permissionQLKH = new ArrayList<>();
        List<PermissionDto> permissionQLNDV = new ArrayList<>();
        List<PermissionDto> permissionQLB = new ArrayList<>();
        List<PermissionDto> permissionQLCH = new ArrayList<>();
        List<PermissionDto> permissionBCKNB = new ArrayList<>();
        List<PermissionDto> permissionBCKNC = new ArrayList<>();
        List<PermissionDto> permissionBCTTND = new ArrayList<>();

        for(int i = 0 ;i < permissionEntityList.size(); i++){
            PermissionDto permissionDto = new PermissionDto();
            permissionDto.setId(permissionEntityList.get(i).getId());
            permissionDto.setPermission(permissionEntityList.get(i).getPermission());
            if(permissionDto.getPermission().contains("ALL")){
                permissionDto.setOrder(0);
            }else if(permissionDto.getPermission().contains("VIEW")){
                permissionDto.setOrder(1);
            }else if(permissionDto.getPermission().contains("CREATE")){
                permissionDto.setOrder(2);
            }else if(permissionDto.getPermission().contains("UPDATE")){
                permissionDto.setOrder(3);
            }else{
                permissionDto.setOrder(4);
            }
            if(permissionDto.getPermission().contains("QLND_")){
                permissionQLND.add(permissionDto);
            }
            if(permissionDto.getPermission().contains("QLNND_")){
                permissionQLNND.add(permissionDto);
            }
            if(permissionDto.getPermission().contains("LSND_")){
                permissionLSND.add(permissionDto);
            }
            if(permissionDto.getPermission().contains("QLKH_")){
                permissionQLKH.add(permissionDto);
            }
            if(permissionDto.getPermission().contains("QLNDV_")){
                permissionQLNDV.add(permissionDto);
            }
            if(permissionDto.getPermission().contains("QLB_")){
                permissionQLB.add(permissionDto);
            }
            if(permissionDto.getPermission().contains("QLCH_")){
                permissionQLCH.add(permissionDto);
            }
            if(permissionDto.getPermission().contains("BCKNB_")){
                permissionBCKNB.add(permissionDto);
            }
            if(permissionDto.getPermission().contains("BCKNC_")){
                permissionBCKNC.add(permissionDto);
            }
            if(permissionDto.getPermission().contains("BCTTND_")){
                permissionBCTTND.add(permissionDto);
            }
        }

        permissionQLND.sort(Comparator.comparing(PermissionDto::getOrder));
        permissionQLNND.sort(Comparator.comparing(PermissionDto::getOrder));
        permissionLSND.sort(Comparator.comparing(PermissionDto::getOrder));
        permissionQLKH.sort(Comparator.comparing(PermissionDto::getOrder));
        permissionQLNDV.sort(Comparator.comparing(PermissionDto::getOrder));
        permissionQLB.sort(Comparator.comparing(PermissionDto::getOrder));
        permissionQLCH.sort(Comparator.comparing(PermissionDto::getOrder));
        permissionBCKNB.sort(Comparator.comparing(PermissionDto::getOrder));
        permissionBCKNC.sort(Comparator.comparing(PermissionDto::getOrder));
        permissionBCTTND.sort(Comparator.comparing(PermissionDto::getOrder));

        permissionGetChildQLND.setName("Quản lý người dùng");
        permissionGetChildQLND.setPermissions(permissionQLND);
        permissionGetChildQLNND.setName("Quản lý nhóm người dùng");
        permissionGetChildQLNND.setPermissions(permissionQLNND);
        permissionGetChildLSND.setName("Lịch sử người dùng");
        permissionGetChildLSND.setPermissions(permissionLSND);
        permissionGetChildQLKH.setName("Quản lý khách hàng");
        permissionGetChildQLKH.setPermissions(permissionQLKH);
        permissionGetChildQLNDV.setName("Quản lý nhóm dịch vụ");
        permissionGetChildQLNDV.setPermissions(permissionQLNDV);
        permissionGetChildQLB.setName("Quản lý box");
        permissionGetChildQLB.setPermissions(permissionQLB);
        permissionGetChildQLCH.setName("Quản lý cấu hình");
        permissionGetChildQLCH.setPermissions(permissionQLCH);
        permissionGetChildBCKNB.setName("Báo cáo kết nối box");
        permissionGetChildBCKNB.setPermissions(permissionBCKNB);
        permissionGetChildBCKNC.setName("Báo cáo kết nối camera");
        permissionGetChildBCKNC.setPermissions(permissionBCKNC);
        permissionGetChildBCTTND.setName("Báo cáo tăng trưởng người dùng");
        permissionGetChildBCTTND.setPermissions(permissionBCTTND);

        permissionQTHT.add(permissionGetChildQLND);
        permissionQTHT.add(permissionGetChildQLNND);
        permissionQTHT.add(permissionGetChildLSND);
        permissionQTTT.add(permissionGetChildQLKH);
        permissionQTTT.add(permissionGetChildQLNDV);
        permissionQTTT.add(permissionGetChildQLB);
        permissionQTTT.add(permissionGetChildQLCH);
        permissionBC.add(permissionGetChildBCKNB);
        permissionBC.add(permissionGetChildBCKNC);
        permissionBC.add(permissionGetChildBCTTND);

        permissionGetQTHT.setName("Quản trị hệ thống");
        permissionGetQTHT.setPermissions(permissionQTHT);
        permissionGetQTTT.setName("Quản trị thông tin");
        permissionGetQTTT.setPermissions(permissionQTTT);
        permissionGetBC.setName("Báo cáo");
        permissionGetBC.setPermissions(permissionBC);

        List<PermissionGetDto> permissionGetDtoList = Arrays.asList( permissionGetQTHT, permissionGetQTTT, permissionGetBC);

        return permissionGetDtoList;
    }

    @Override
    public List<PermissionDto> findAll() {
        List<PermissionEntity> permissions = permissionRepository.findAll();
        List<PermissionDto> permissionsDto = permissions.stream().map(p -> new PermissionDto(p)).collect(Collectors.toList());
        return permissionsDto;
    }
}
