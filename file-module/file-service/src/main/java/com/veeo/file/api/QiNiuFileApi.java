package com.veeo.file.api;

import com.qiniu.storage.model.FileInfo;
import com.veeo.common.util.Result;
import com.veeo.file.client.QiNiuFileClient;
import com.veeo.file.service.QiNiuFileService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;


@RestController
@RequestMapping("/api/QiNiuFile")
public class QiNiuFileApi implements QiNiuFileClient {

    @Resource
    private QiNiuFileService qiNiuFileService;


    @Override
    public Result<String> getToken() {
        return qiNiuFileService.getToken();
    }

    @Override
    public Result<String> uploadFile(File file) {
        return qiNiuFileService.uploadFile(file);
    }

    @Override
    public void deleteFile(String url) {
        qiNiuFileService.deleteFile(url);
    }

    @Override
    public Result<FileInfo> getFileInfo(String url) {
        return qiNiuFileService.getFileInfo(url);
    }
}
