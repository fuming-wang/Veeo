package com.veeo.file.client;

import com.veeo.common.entity.File;
import com.veeo.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;


@FeignClient(value = "file-service", contextId = "file-client", path = "/api/file")
public interface FileClient {

    @RequestMapping("/save")
    Result<Long> save(@RequestParam String fileKey, @RequestParam Long userId);

    @RequestMapping("/generatePhoto")
    Result<Long> generatePhoto(@RequestParam Long fileId, @RequestParam Long userId);

    @RequestMapping("/getFileTrustUrl")
    Result<File> getFileTrustUrl(@RequestParam Long fileId);

    @RequestMapping("/getById")
    Result<File> getById(@RequestParam Long fileId);

    @RequestMapping("/listByIds")
    Result<Collection<File>> listByIds(@RequestParam Collection<Long> fileIds);

}
