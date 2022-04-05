package org.thingsboard.server.dft.enduser.dao.box;

import org.json.JSONException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.enduser.dto.box.BoxDetailDto;
import org.thingsboard.server.dft.enduser.dto.box.BoxEditDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.util.List;
import java.util.UUID;

public interface BoxDao {

    BoxEditDto create(BoxEditDto box, UUID tbDeviceId, SecurityUser securityUser) throws JSONException;

    BoxEditDto update(BoxEditDto box, SecurityUser securityUser);

    PageData<BoxDetailDto> findAllBySearchText(Pageable pageable, Boolean status,
                                               String searchText, SecurityUser securityUser, boolean fullOption);

    BoxDetailDto getBoxDetailById(UUID id);

    BoxDetailDto getBoxDetailByTenantIdAndTbDeviceId(UUID tenantId, UUID tbDeviceId);

    DeviceId deleteById(UUID id, UUID tenantId);

    boolean checkExistSerialNumber(String serialNumber);

    List<BoxDetailDto> findAll(UUID tenantId, Sort sort);

    boolean existsByBoxNameAndTenantId(String boxName, UUID tenantId);

    boolean existsByBoxNameAndTenantIdAndIdNot(String boxName, UUID tenantId, UUID id);
}
