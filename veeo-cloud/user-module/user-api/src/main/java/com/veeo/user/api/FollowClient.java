package com.veeo.user.api;


import com.veeo.common.entity.vo.BasePage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

@FeignClient(value = "user-service", contextId = "follow-client", path = "/api/follow")
public interface FollowClient {

    @RequestMapping("/getFollowCount")
    int getFollowCount(@RequestParam Long userId);

    @RequestMapping("/getFansCount")
    int getFansCount(@RequestParam Long userId);

    @RequestMapping("/getFollow")
    Collection<Long> getFollow(@RequestParam Long userId, @RequestBody BasePage basePage);

    @RequestMapping("/getFollow2")
    Collection<Long> getFollow(@RequestParam Long userId);

    @RequestMapping("/getFans")
    Collection<Long> getFans(@RequestParam Long userId, @RequestBody BasePage basePage);

    @RequestMapping("/getFans2")
    Collection<Long> getFans(@RequestParam Long userId);

    @RequestMapping("/follows")
    Boolean follows(@RequestParam Long followId, @RequestParam Long userId);

    @RequestMapping("/isFollows")
    Boolean isFollows(@RequestParam Long followId, @RequestParam Long userId);


}
