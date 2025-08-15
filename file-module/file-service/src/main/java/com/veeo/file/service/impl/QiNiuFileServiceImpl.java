package com.veeo.file.service.impl;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import com.veeo.file.config.QiNiuConfig;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.file.service.QiNiuFileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;


@Service
@Slf4j
public class QiNiuFileServiceImpl implements QiNiuFileService {

    @Resource
    private QiNiuConfig qiNiuConfig;

    @Override
    public Result<String> getToken() {
        return ResultUtil.getSucRet(qiNiuConfig.videoUploadToken());
    }

    @Override
    public Result<String> uploadFile(File file) {
        Configuration cfg = new Configuration(Region.region2());
        UploadManager uploadManager = new UploadManager(cfg);
        String errorMsg;
        try {
            Response response = uploadManager.put(file, null, qiNiuConfig.videoUploadToken());
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            return ResultUtil.getSucRet(putRet.key);
        } catch (QiniuException ex) {
            errorMsg = ex.toString();
            log.error(errorMsg);
            if (ex.response != null) {
                System.err.println(ex.response);
                try {
                    String body = ex.response.toString();
                    log.error(body);
                } catch (Exception ex2) {
                    log.error(ex2.getMessage());
                }
            }
        }
        return ResultUtil.getFailRet(errorMsg);
    }

    @Override
    @Async
    public void deleteFile(String url) {
        Configuration cfg = new Configuration(Region.region0());
        String bucket = qiNiuConfig.getBucketName();
        Auth auth = qiNiuConfig.buildAuth();
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            bucketManager.delete(bucket, url);
        } catch (QiniuException ex) {
            //如果遇到异常，说明删除失败
            log.error("错误码 {}", ex.code());
            log.error("异常信息: {}", ex.response.toString());
        }
    }

    @Override
    public Result<FileInfo> getFileInfo(String url) {
        Configuration cfg = new Configuration(Region.region0());
        Auth auth = qiNiuConfig.buildAuth();
        String bucket = qiNiuConfig.getBucketName();
        BucketManager bucketManager = new BucketManager(auth, cfg);
        try {
            return ResultUtil.getSucRet(bucketManager.stat(bucket, url));
        } catch (QiniuException ex) {
            log.error(ex.response.toString());
        }
        return ResultUtil.getFailRet("获取文件信息错误");
    }

}
