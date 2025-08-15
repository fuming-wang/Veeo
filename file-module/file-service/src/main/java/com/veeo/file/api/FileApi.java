package com.veeo.file.api;

import com.veeo.common.entity.File;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.file.client.FileClient;
import com.veeo.file.handler.FileHandler;
import com.veeo.file.service.FileService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;


@RequestMapping("/api/file")
@RestController
public class FileApi implements FileClient {

    @Resource
    private FileService fileService;

    @Resource
    private FileHandler fileHandler;


    @Override
    public Result<Long> save(String fileKey, Long userId) {
        return fileHandler.save(fileKey, userId);
    }

    @Override
    public Result<Long> generatePhoto(Long fileId, Long userId) {
        return fileHandler.generatePhoto(fileId, userId);
    }

    @Override
    public Result<File> getFileTrustUrl(Long fileId) {
        return fileHandler.getFileTrustUrl(fileId);
    }

    @Override
    public Result<File> getById(Long fileId) {
        return ResultUtil.getSucRet(fileService.getById(fileId));
    }

    @Override
    public Result<Collection<File>> listByIds(Collection<Long> fileIds) {
        return ResultUtil.getSucRet(fileService.listByIds(fileIds));
    }

}
