package com.veeo.file.handler;

import com.qiniu.storage.model.FileInfo;
import com.veeo.common.config.LocalCache;
import com.veeo.file.config.QiNiuConfig;
import com.veeo.common.entity.File;
import com.veeo.common.exception.BaseException;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.file.constant.RedisConstant;
import com.veeo.file.service.FileService;
import com.veeo.file.service.QiNiuFileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

/**
 * @ClassName FileHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/26 21:53
 * @Version 1.0.0
 */
@Service
public class FileHandler {

    @Resource
    private QiNiuFileService qiNiuFileService;

    @Resource
    private FileService fileService;

    @Resource
    private LocalCache localCache;

    @Resource
    private RedisCacheUtil redisCacheUtil;

    public Result<Long> save(String fileKey, Long userId) {
        Result<FileInfo> videoFileInfoResult = qiNiuFileService.getFileInfo(fileKey);
        if (videoFileInfoResult.isFailed()) {
            throw new BaseException(videoFileInfoResult.getMessage());
        }
        FileInfo videoFileInfo = videoFileInfoResult.getData();
        File videoFile = new File();
        String type = videoFileInfo.mimeType;
        videoFile.setFileKey(fileKey);
        videoFile.setFormat(type);
        videoFile.setType(type.contains("video") ? "视频" : "图片");
        videoFile.setUserId(userId);
        videoFile.setIsDeleted(false);
        videoFile.setSize(videoFileInfo.fsize);
        fileService.save(videoFile);
        return ResultUtil.getSucRet(videoFile.getId());
    }

    public Result<Long> generatePhoto(Long fileId, Long userId) {
        File file = fileService.getById(fileId);
        String fileKey = file.getFileKey() + "?vframe/jpg/offset/1";
        File fileInfo = new File();
        fileInfo.setFileKey(fileKey);
        fileInfo.setFormat("image/*");
        fileInfo.setType("图片");
        fileInfo.setUserId(userId);
        fileInfo.setIsDeleted(false);
        fileInfo.setSize(file.getSize());
        fileService.save(fileInfo);
        return ResultUtil.getSucRet(fileInfo.getId());
    }

    public Result<File> getFileTrustUrl(Long fileId) {
        File file;
        file = (File) redisCacheUtil.get(RedisConstant.FILE_CACHE + fileId);
        if (Objects.isNull(file)) {
            file = fileService.getById(fileId);
            // 如果file为null呢, 那也缓存null
            redisCacheUtil.set(RedisConstant.FILE_CACHE + fileId, file, 600);
        }
        if (Objects.isNull(file)) {
            throw new BaseException("未找到该文件");
        }
        String s = UUID.randomUUID().toString();
        localCache.put(s, true);
        String url = QiNiuConfig.CNAME + "/" + file.getFileKey();

        if (url.contains("?")) {
            url = url + "&uuid=" + s;
        } else {
            url = url + "?uuid=" + s;
        }
        file.setFileKey(url);
        return ResultUtil.getSucRet(file);
    }
}
