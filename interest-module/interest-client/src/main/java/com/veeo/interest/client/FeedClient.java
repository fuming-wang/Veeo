package com.veeo.interest.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;


@FeignClient(value = "interest-service", contextId = "feed-client", path = "/api/feed")
public interface FeedClient {


    /**
     * 推入发件箱
     * @param userId 发件箱用户id
     * @param videoId 视频id
     */
    @RequestMapping("/pusOutBoxFeed")
    void pushOutBoxFeed(@RequestParam Long userId, @RequestParam Long videoId, @RequestParam Long time);

    /**
     * 推入收件箱
     * @param userId 收件箱用户id
     * @param videoId 视频id
     */
    @RequestMapping("/pushInBoxFeed")
    void pushInBoxFeed(@RequestParam Long userId, @RequestParam Long videoId, @RequestParam Long time);

    /**
     * 删除发件箱
     * 当前用户删除视频时调用, 删除当前用户的发件箱中视频以及粉丝下的收件箱
     * @param userId 当前用户
     * @param fans 粉丝id
     * @param videoId 视频id 需要删除的
     */
    @RequestMapping("/deleteOutBoxFeed")
    void deleteOutBoxFeed(@RequestParam Long userId, @RequestParam Collection<Long> fans, @RequestParam Long videoId);

    /**
     * 删除收件箱
     * 当前用户取关用户时调用
     * 删除自己收件箱中的videoIds
     * @param userId 用户id
     * @param videoIds 关注人发的视频id
     */
    @RequestMapping("/deleteInBoxFeed")
    void deleteInBoxFeed(@RequestParam Long userId, @RequestParam List<Long> videoIds);

    /**
     * 初始化关注流-拉模式 with TTL
     * @param userId 用户id
     */
    @RequestMapping("/initFollowFeed")
    void initFollowFeed(@RequestParam Long userId, @RequestParam Collection<Long> followIds);
}
