package com.veeo.file.client;

import com.qiniu.storage.model.FileInfo;
import com.veeo.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;


@FeignClient(value = "file-service", contextId = "QiNiuFile-client", path = "/api/QiNiuFile")
public interface QiNiuFileClient {


    @RequestMapping("/getToken")
    Result<String> getToken();

    @RequestMapping("/uploadFile")
    Result<String> uploadFile(@RequestBody File file);

    @RequestMapping("/deleteFile")
    void deleteFile(@RequestParam String url);

    @RequestMapping("/getFileInfo")
    Result<FileInfo> getFileInfo(@RequestParam String url);
}
