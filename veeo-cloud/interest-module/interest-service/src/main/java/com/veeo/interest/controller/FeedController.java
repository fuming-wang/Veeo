package com.veeo.interest.controller;

import com.veeo.interest.api.FeedClient;
import com.veeo.interest.service.FeedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/feed")
public class FeedController implements FeedClient {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @Override
    public void pushOutBoxFeed(Long userId, Long videoId, Long time) {
        if(userId == null || videoId == null || time == null) {
            log.error("userId and videoId and time are null");
        }
        feedService.pushOutBoxFeed(userId, videoId, time);
    }

    @Override
    public void pushInBoxFeed(Long userId, Long videoId, Long time) {
        if(userId == null || videoId == null || time == null) {
            log.error("userId and videoId and time are null");
        }
        feedService.pushInBoxFeed(userId, videoId, time);
    }

    @Override
    public void deleteOutBoxFeed(Long userId, Collection<Long> fans, Long videoId) {
        if(userId == null || videoId == null) {
            log.error("userId and videoId and time are null");
        }

        feedService.deleteOutBoxFeed(userId, fans, videoId);
    }

    @Override
    public void deleteInBoxFeed(Long userId, List<Long> videoIds) {
        if(userId == null || videoIds == null) {
            log.error("userId and videoIds are null");
        }
        feedService.deleteInBoxFeed(userId, videoIds);
    }

    @Override
    public void initFollowFeed(Long userId, Collection<Long> followIds) {
        if(userId == null || followIds == null) {
            log.error("userId and followIds are null");
        }
        feedService.initFollowFeed(userId, followIds);
    }
}
