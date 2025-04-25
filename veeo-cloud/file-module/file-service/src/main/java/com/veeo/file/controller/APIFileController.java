package com.veeo.file.controller;

import com.veeo.common.entity.File;
import com.veeo.file.api.FileClient;
import com.veeo.file.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Slf4j
@RequestMapping("/api/file")
@RestController
public class APIFileController implements FileClient {

    private final FileService fileService;

    public APIFileController(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public Long save(String fileKey, Long userId) {
        if(fileKey == null || fileKey.isEmpty()) {
            log.error("fileKey or userId is null or empty");
        }
        return fileService.save(fileKey, userId);
    }

    @Override
    public Long generatePhoto(Long fileId, Long userId) {
        if(userId == null || fileId == null) {
            log.error("userId or fileId is null or empty");
        }
        return fileService.generatePhoto(fileId, userId);
    }

    @Override
    public File getFileTrustUrl(Long fileId) {
        if(fileId == null) {
            log.error("fileId is null");
        }
        return fileService.getFileTrustUrl(fileId);
    }

    @Override
    public File getById(Long fileId) {
        if(fileId == null) {
            log.error("fileId is null");
        }
        return fileService.getById(fileId);
    }

    @Override
    public Collection<File> listByIds(Collection<Long> fileIds) {
        if(fileIds == null || fileIds.isEmpty()) {
            log.error("fileIds is null or empty");
        }
        Collection<File> files = fileService.listByIds(fileIds);
        if(files.isEmpty()) {
            log.error("fileIds is empty");
        }
        return files;
//        return fileService.listByIds(fileIds);
    }

}
