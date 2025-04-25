package com.veeo.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiniu.storage.model.FileInfo;
import com.veeo.common.config.LocalCache;
import com.veeo.common.config.QiNiuConfig;
import com.veeo.common.entity.File;
import com.veeo.common.exception.BaseException;
import com.veeo.file.mapper.FileMapper;
import com.veeo.file.service.FileService;
import com.veeo.file.service.QiNiuFileService;
import org.springframework.stereotype.Service;


import java.util.Objects;
import java.util.UUID;

/**
 *  服务实现类
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

    private final QiNiuFileService qiNiuFileService;

    public FileServiceImpl(QiNiuFileService qiNiuFileService) {
        this.qiNiuFileService = qiNiuFileService;
    }

    @Override
    public Long save(String fileKey,Long userId) {

        // 判断文件
        FileInfo videoFileInfo = qiNiuFileService.getFileInfo(fileKey);

        if (videoFileInfo == null){
            throw new IllegalArgumentException("参数不正确");
        }

        File videoFile = new File();
        String type = videoFileInfo.mimeType;
        videoFile.setFileKey(fileKey);
        videoFile.setFormat(type);
        videoFile.setType(type.contains("video") ? "视频" : "图片");
        videoFile.setUserId(userId);
        videoFile.setSize(videoFileInfo.fsize);
        save(videoFile);

        return videoFile.getId();
    }

    @Override
    public Long generatePhoto(Long fileId,Long userId) {
        File file = getById(fileId);;
        String fileKey = file.getFileKey() + "?vframe/jpg/offset/1";
        File fileInfo = new File();
        fileInfo.setFileKey(fileKey);
        fileInfo.setFormat("image/*");
        fileInfo.setType("图片");
        fileInfo.setUserId(userId);
        save(fileInfo);
        return fileInfo.getId();
    }

    @Override
    public File getFileTrustUrl(Long fileId) {
        File file = getById(fileId);
        if (Objects.isNull(file)) {
            throw new BaseException("未找到该文件");
        }
        String s = UUID.randomUUID().toString();
        LocalCache.put(s,true);
        String url = QiNiuConfig.CNAME + "/" + file.getFileKey();

        if (url.contains("?")){
            url = url+"&uuid="+s;
        } else {
            url = url+"?uuid="+s;
        }
        file.setFileKey(url);
        return file;
    }
}
