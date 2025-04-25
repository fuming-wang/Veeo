package com.veeo.interest.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;


@FeignClient(value = "interest-service", contextId = "feed-client", path = "/api/feed")
public interface FeedClient {

    @RequestMapping("/pusOutBoxFeed")
    void pushOutBoxFeed(@RequestParam Long userId, @RequestParam Long videoId, @RequestParam Long time);

    @RequestMapping("/pushInBoxFeed")
    void pushInBoxFeed(@RequestParam Long userId, @RequestParam Long videoId, @RequestParam Long time);

    @RequestMapping("/deleteOutBoxFeed")
    void deleteOutBoxFeed(@RequestParam Long userId, @RequestParam Collection<Long> fans, @RequestParam Long videoId);

    @RequestMapping("/deleteInBoxFeed")
    void deleteInBoxFeed(@RequestParam Long userId, @RequestParam List<Long> videoIds);

    @RequestMapping("/initFollowFeed")
    void initFollowFeed(@RequestParam Long userId, @RequestParam Collection<Long> followIds);
}
