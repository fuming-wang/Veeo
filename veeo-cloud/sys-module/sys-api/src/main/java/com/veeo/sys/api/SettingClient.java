package com.veeo.sys.api;


import com.veeo.common.entity.Setting;
import com.veeo.common.query.QueryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "sys-service", contextId = "setting-client", path = "/api/setting")
public interface SettingClient {

    @RequestMapping("/getById")
    Setting getById(@RequestParam Long id);

    @RequestMapping("/list")
    List<Setting> list(@RequestBody QueryDTO queryDTO);


    @RequestMapping("/list2")
    List<Setting> list();

}
