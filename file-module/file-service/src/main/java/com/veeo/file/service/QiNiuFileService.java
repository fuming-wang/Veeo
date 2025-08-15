package com.veeo.file.service;

import com.qiniu.storage.model.FileInfo;
import com.veeo.common.util.Result;

import java.io.File;


public interface QiNiuFileService extends FileCloudService {


    /**
     * 获取签名
     */
    Result<String> getToken();

    /**
     * 上传文件
     */
    Result<String> uploadFile(File file);

    /**
     * 删除文件
     */
    void deleteFile(String url);

    /**
     * 获取文件信息
     */
    Result<FileInfo> getFileInfo(String url);
}
