package com.veeo.file.controller;

import com.qiniu.storage.model.FileInfo;
import com.veeo.file.api.QiNiuFileClient;
import com.veeo.file.service.QiNiuFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.io.File;

@Slf4j
@RestController
@RequestMapping("/api/QiNiuFile")
public class APIQiNiuFileController implements QiNiuFileClient {

    private final QiNiuFileService qiNiuFileService;

    public APIQiNiuFileController(QiNiuFileService qiNiuFileService) {
        this.qiNiuFileService = qiNiuFileService;
    }

    @Override
    public String getToken() {
        return qiNiuFileService.getToken();
    }

    @Override
    public String uploadFile(File file) {
        if (file == null) {
            log.error("file is null");
        }
        return qiNiuFileService.uploadFile(file);
    }

    @Override
    public void deleteFile(String url) {
        if (url == null) {
            log.error("url is null");
        }
        qiNiuFileService.deleteFile(url);
    }

    @Override
    public FileInfo getFileInfo(String url) {
        if (url == null) {
            log.error("url is null");
        }
        return qiNiuFileService.getFileInfo(url);
    }
}
