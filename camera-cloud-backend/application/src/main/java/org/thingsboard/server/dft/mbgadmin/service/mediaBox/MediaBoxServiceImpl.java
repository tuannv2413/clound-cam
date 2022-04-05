package org.thingsboard.server.dft.mbgadmin.service.mediaBox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thingsboard.server.common.data.page.PageData;
import org.thingsboard.server.dft.mbgadmin.dao.mediaBox.MediaBoxDao;
import org.thingsboard.server.dft.mbgadmin.dto.mediaBox.MediaBoxDetailDto;
import org.thingsboard.server.dft.mbgadmin.dto.mediaBox.MediaBoxEditDto;
import org.thingsboard.server.service.security.model.SecurityUser;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.UUID;

@Service
public class MediaBoxServiceImpl implements MediaBoxService {
    private final MediaBoxDao mediaBoxDao;
    @Value("${install.data_dir:application/src/main/data}")
    private String dataDir;
  @Autowired
  public MediaBoxServiceImpl(MediaBoxDao mediaBoxDao) {
    this.mediaBoxDao = mediaBoxDao;
  }

  @Override
  public MediaBoxEditDto createOrUpdate(MediaBoxEditDto mediaBoxEditDto, SecurityUser securityUser) {
      mediaBoxEditDto.setCreatedTime(new Date().getTime());
      mediaBoxEditDto.setUpdatedTime(new Date().getTime());
      mediaBoxEditDto.setCreatedBy(securityUser.getUuidId());
      mediaBoxEditDto.setUpdatedBy(securityUser.getUuidId());
      return mediaBoxDao.createOrUpdate(mediaBoxEditDto, securityUser);
  }

    @Override
    public PageData<MediaBoxEditDto> getPage(Pageable pageable, String textSearch, String type, String status, String firmwareVersion) {
        return mediaBoxDao.getPage(pageable, textSearch, type, status, firmwareVersion);
    }

    @Override
    public PageData<MediaBoxEditDto> getCustomPageBySearch(Pageable pageable, String textSearch, String type, String status, String firmwareVersion, Boolean active) {
        return mediaBoxDao.getCustomPageBySearch(pageable, textSearch, type, status, firmwareVersion, active);
    }

  @Override
  public PageData<MediaBoxEditDto> getPageBySearch(Pageable pageable, String textSearch, String type, String status, String firmwareVersion, Boolean active) {
    return mediaBoxDao.getPageBySearch(pageable, textSearch, type, status, firmwareVersion, active);
  }

  @Override
    public MediaBoxDetailDto getById(UUID id) {
        return mediaBoxDao.getById(id);
    }

    @Override
    public void deleteById(UUID id) {
        mediaBoxDao.deleteById(id);
    }

  @Override
  public boolean checkExistSerialNumber(String serialNumber) {
    return mediaBoxDao.checkSerialNumberExist(serialNumber);
  }

    @Override
    public boolean checkExistOriginSerialNumberAndIdNot(String originSerialNumber, UUID id) {
        return mediaBoxDao.checkOriginSerialNumberExistAndIdNot(originSerialNumber, id);
    }

    @Override
    public boolean checkExistOriginSerialNumber(String originSerialNumber) {
      return mediaBoxDao.checkOriginSerialNumberExist(originSerialNumber);
    }

    @Override
    public Resource downloadSample(String fileName) {
        String trueSavePath = dataDir + "/static"
                + File.separator;
        try {
            Path file = Paths.get(trueSavePath)
                    .resolve(fileName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e){
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}
