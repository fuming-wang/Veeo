package com.veeo.file.api;

import com.veeo.common.entity.File;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.Collection;


@FeignClient(value = "file-service", contextId = "file-client", path = "/api/file")
public interface FileClient {

    @RequestMapping("/save")
    Long save(@RequestParam String fileKey, @RequestParam Long userId);

    @RequestMapping("/generatePhoto")
    Long generatePhoto(@RequestParam Long fileId, @RequestParam Long userId);

    @RequestMapping("/getFileTrustUrl")
    File getFileTrustUrl(@RequestParam Long fileId);

    @RequestMapping("/getById")
    File getById(@RequestParam Long fileId);

    @RequestMapping("/listByIds")
    Collection<File> listByIds(@RequestParam Collection<Long> fileIds);

}
