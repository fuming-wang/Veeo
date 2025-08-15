package com.veeo.user.client;


import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

@FeignClient(value = "user-service", contextId = "follow-client", path = "/api/follow")
public interface FollowClient {

    @RequestMapping("/getFollowCount")
    Result<Integer> getFollowCount(@RequestParam Long userId);

    @RequestMapping("/getFansCount")
    Result<Integer> getFansCount(@RequestParam Long userId);

    @RequestMapping("/getFollow")
    Result<Collection<Long>> getFollow(@RequestParam Long userId, @RequestBody BasePage basePage);

    @RequestMapping("/getFollow2")
    Result<Collection<Long>> getFollow(@RequestParam Long userId);

    @RequestMapping("/getFans")
    Result<Collection<Long>> getFans(@RequestParam Long userId, @RequestBody BasePage basePage);

    @RequestMapping("/getFans2")
    Result<Collection<Long>> getFans(@RequestParam Long userId);

    @RequestMapping("/follows")
    Result<Boolean> follows(@RequestParam Long followId, @RequestParam Long userId);

    @RequestMapping("/isFollows")
    Result<Boolean> isFollows(@RequestParam Long followId, @RequestParam Long userId);

}
