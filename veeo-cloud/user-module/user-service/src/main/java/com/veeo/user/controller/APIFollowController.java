package com.veeo.user.controller;


import com.veeo.common.entity.vo.BasePage;
import com.veeo.user.api.FollowClient;
import com.veeo.user.service.FollowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/follow")
public class APIFollowController implements FollowClient {

    private final FollowService followService;

    public APIFollowController(FollowService followService) {
        this.followService = followService;
    }

    @Override
    public int getFollowCount(Long userId) {
        if(userId == null){
            log.error("getFollowCount userId is null");
        }
        return followService.getFollowCount(userId);
    }

    @Override
    public int getFansCount(Long userId) {
        if(userId == null){
            log.error("getFansCount userId is null");
        }
        return followService.getFansCount(userId);
    }

    @Override
    public Collection<Long> getFollow(Long userId, BasePage basePage) {
        if(userId == null || basePage == null){
            log.error("getFollow userId and basePage is null");
        }

        return followService.getFollow(userId, basePage);
    }

    @Override
    public Collection<Long> getFollow(Long userId) {
        if(userId == null) {
            log.error("getFollow userId is null");
        }
        return followService.getFollow(userId, null);
    }

    @Override
    public Collection<Long> getFans(Long userId, BasePage basePage) {
        if(userId == null || basePage == null) {
            log.error("getFans userId and basePage is null");
        }
        return followService.getFans(userId, basePage);
    }

    @Override
    public Collection<Long> getFans(Long userId) {
        if(userId == null) {
            log.error("getFans userId is null");
        }
        return followService.getFans(userId, null);
    }

    @Override
    public Boolean follows(Long followId, Long userId) {
        if(userId == null || followId == null) {
            log.error("follows userId and followId is null");
        }
        return followService.follows(followId, userId);
    }

    @Override
    public Boolean isFollows(Long followId, Long userId) {
        if(userId == null || followId == null) {
            log.error("isFollows userId and followId is null");
        }
        return followService.isFollows(followId, userId);
    }
}
