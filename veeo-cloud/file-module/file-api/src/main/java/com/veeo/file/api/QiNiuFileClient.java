package com.veeo.file.api;

import com.qiniu.storage.model.FileInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;


@FeignClient(value = "file-service", contextId = "QiNiuFile-client", path = "/api/QiNiuFile")
public interface QiNiuFileClient {


    @RequestMapping("/getToken")
    String getToken();

    @RequestMapping("/uploadFile")
    String uploadFile(@RequestBody File file);


    @RequestMapping("/deleteFile")
    void deleteFile(@RequestParam String url);

    @RequestMapping("/getFileInfo")
    FileInfo getFileInfo(@RequestParam String url);
}
