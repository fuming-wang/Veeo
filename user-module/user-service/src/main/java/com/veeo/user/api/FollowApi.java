package com.veeo.user.api;


import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.user.client.FollowClient;
import com.veeo.user.service.FollowService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/api/follow")
public class FollowApi implements FollowClient {

    @Resource
    private FollowService followService;


    @Override
    public Result<Integer> getFollowCount(Long userId) {
        return ResultUtil.getSucRet(followService.getFollowCount(userId));
    }

    @Override
    public Result<Integer> getFansCount(Long userId) {
        return ResultUtil.getSucRet(followService.getFansCount(userId));
    }

    @Override
    public Result<Collection<Long>> getFollow(Long userId, BasePage basePage) {
        return ResultUtil.getSucRet(followService.getFollow(userId, basePage));
    }

    @Override
    public Result<Collection<Long>> getFollow(Long userId) {
        return ResultUtil.getSucRet(followService.getFollow(userId, null));
    }

    @Override
    public Result<Collection<Long>> getFans(Long userId, BasePage basePage) {
        return ResultUtil.getSucRet(followService.getFans(userId, basePage));
    }

    @Override
    public Result<Collection<Long>> getFans(Long userId) {
        return ResultUtil.getSucRet(followService.getFans(userId, null));
    }

    @Override
    public Result<Boolean> follows(Long followId, Long userId) {
        return ResultUtil.getSucRet(followService.follows(followId, userId));
    }

    @Override
    public Result<Boolean> isFollows(Long followId, Long userId) {
        return ResultUtil.getSucRet(followService.isFollows(followId, userId));
    }
}
