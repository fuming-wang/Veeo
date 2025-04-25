package com.veeo.video.api;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.veeo.common.entity.video.Type;
import com.veeo.common.query.QueryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;


@FeignClient(value = "video-service", contextId = "type-client", path = "/api/type")
public interface TypeClient {


    @RequestMapping("/getLabels")
    List<String> getLabels(@RequestParam Long typeId);

    @RequestMapping("/random10Labels")
    List<String> random10Labels();

    @RequestMapping("/listByIds")
    Collection<Type> listByIds(@RequestParam Collection<Long> ids);

    @RequestMapping("/list")
    List<Type> list(@RequestBody QueryDTO queryDTO);
}
