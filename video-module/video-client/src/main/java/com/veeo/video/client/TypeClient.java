package com.veeo.video.client;

import com.veeo.common.entity.video.Type;
import com.veeo.common.query.QueryDTO;
import com.veeo.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;


@FeignClient(value = "video-service", contextId = "type-client", path = "/api/type")
public interface TypeClient {


    @RequestMapping("/getLabels")
    Result<List<String>> getLabels(@RequestParam Long typeId);

    @RequestMapping("/random10Labels")
    Result<List<String>> random10Labels();

    @RequestMapping("/listByIds")
    Result<Collection<Type>> listByIds(@RequestParam Collection<Long> ids);

    @RequestMapping("/list")
    Result<List<Type>> list(@RequestBody QueryDTO queryDTO);
}
