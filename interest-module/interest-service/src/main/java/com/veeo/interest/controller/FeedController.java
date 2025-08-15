package com.veeo.interest.controller;

import com.veeo.interest.client.FeedClient;
import com.veeo.interest.service.FeedService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/feed")
public class FeedController implements FeedClient {

    @Resource
    private FeedService feedService;

    @Override
    public void pushOutBoxFeed(Long userId, Long videoId, Long time) {
        feedService.pushOutBoxFeed(userId, videoId, time);
    }

    @Override
    public void pushInBoxFeed(Long userId, Long videoId, Long time) {
        feedService.pushInBoxFeed(userId, videoId, time);
    }

    @Override
    public void deleteOutBoxFeed(Long userId, Collection<Long> fans, Long videoId) {
        feedService.deleteOutBoxFeed(userId, fans, videoId);
    }

    @Override
    public void deleteInBoxFeed(Long userId, List<Long> videoIds) {
        feedService.deleteInBoxFeed(userId, videoIds);
    }

    @Override
    public void initFollowFeed(Long userId, Collection<Long> followIds) {
        feedService.initFollowFeed(userId, followIds);
    }
}
