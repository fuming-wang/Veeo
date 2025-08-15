package com.veeo.user.client;


import com.veeo.common.entity.user.UserSubscribe;
import com.veeo.common.query.QueryDTO;
import com.veeo.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "user-service", contextId = "userSubscribe-client", path = "/api/userSubscribe")
public interface UserSubscribeClient {

    @RequestMapping("/remove")
    Result<Boolean> remove(@RequestBody QueryDTO queryDTO);

    @RequestMapping("/saveBatch")
    Result<Boolean> saveBatch(@RequestBody Collection<UserSubscribe> entityList);

    @RequestMapping("/list")
    Result<List<UserSubscribe>> list(@RequestBody QueryDTO queryDTO);

}
