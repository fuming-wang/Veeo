package com.veeo.user.api;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.veeo.common.entity.user.UserSubscribe;
import com.veeo.common.query.QueryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "user-service", contextId = "userSubscribe-client", path = "/api/userSubscribe")
public interface UserSubscribeClient {

    @RequestMapping("/remove")
    boolean remove(@RequestBody QueryDTO queryDTO);


    @RequestMapping("/saveBatch")
    boolean saveBatch(@RequestBody Collection<UserSubscribe> entityList);

    @RequestMapping("/list")
    List<UserSubscribe> list(@RequestBody QueryDTO queryDTO);

}
