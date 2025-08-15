package com.veeo.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.entity.user.Follow;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.exception.BaseException;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.common.util.Result;
import com.veeo.interest.client.FeedClient;
import com.veeo.user.constant.RedisConstant;
import com.veeo.user.mapper.FollowMapper;
import com.veeo.user.service.FollowService;
import com.veeo.video.client.VideoClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户关注功能实现类
 **/
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Resource
    private FeedClient feedClient;

    @Resource
    private FollowMapper followMapper;

    @Resource
    private VideoClient videoClient;

    @Resource
    private RedisCacheUtil redisCacheUtil;


    /**
     * 获取关注数量. 可以利用redis缓存避免每次都有查询数据库
     **/
    @Override
    public int getFollowCount(Long userId) {
        // 查询缓存
        Integer num = (Integer) redisCacheUtil.get(RedisConstant.USER_FOLLOW_NUMBER_CACHE + userId);
        if (num == null) {
            // 查询数据库
            num = (int) count(new LambdaQueryWrapper<Follow>().select(Follow::getId).eq(Follow::getUserId, userId));
            //  构建缓存
            redisCacheUtil.set(RedisConstant.USER_FOLLOW_NUMBER_CACHE + userId, num, 600);
        }
        return num;
    }

    /**
     * 获取粉丝数。 可以利用redis缓存避免每次都有查询数据库
     **/
    @Override
    public int getFansCount(Long userId) {
        // todo 用户的粉丝数量越多是不是缓存保存的时间越长呢?
        // 查询缓存
        Integer num = (Integer) redisCacheUtil.get(RedisConstant.USER_FANS_NUMBER_CACHE + userId);
        if (num == null) {
            // 查询数据库
            num = (int) count(new LambdaQueryWrapper<Follow>().select(Follow::getId).eq(Follow::getFollowId, userId));
            // 构建缓存
            redisCacheUtil.set(RedisConstant.USER_FANS_NUMBER_CACHE, num, 600);
        }
        return num;
    }

    @Override
    public Collection<Long> getFollow(Long userId, BasePage basePage) {
        if (basePage == null) {
            basePage = new BasePage();
        }
        // 查询缓存
        String key = RedisConstant.USER_FOLLOW + userId + ":" + basePage.getPage() + ":" + basePage.getLimit();
        List<Follow> follows = (List<Follow>) redisCacheUtil.get(key);
        // 缓存中不存在或者为空
        if (follows == null || follows.isEmpty()) {
            follows = page(basePage.followPage(), new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, userId).orderByDesc(Follow::getGmtCreated)).getRecords();
            redisCacheUtil.set(key, follows, 600);
        }
        return follows.stream().map(Follow::getId).collect(Collectors.toList());
    }


    @Override
    public Collection<Long> getFans(Long userId, BasePage basePage) {
        if (basePage == null) {
            basePage = new BasePage();
        }
        String key = RedisConstant.USER_FANS + userId + ":" + basePage.getPage() + ":" + basePage.getLimit();
        List<Follow> follows = (List<Follow>) redisCacheUtil.get(key);
        if (follows == null || follows.isEmpty()) {
            follows = page(basePage.followPage(), new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId, userId)).getRecords();
            redisCacheUtil.set(key, follows, 600);
        }
        return follows.stream().map(Follow::getUserId).collect(Collectors.toList());
    }

    @Override
    public Boolean follows(Long followsId, Long userId) {

        if (followsId.equals(userId)) {
            throw new BaseException("你不能关注自己");
        }
        Follow follow;
        follow = (Follow) redisCacheUtil.get(RedisConstant.USER_FOLLOW + userId + ":" + followsId);
        if (follow == null) {
            follow = followMapper.select(followsId, userId);
        }
        if (follow != null && follow.getIsDeleted()) {
            // 重新关注
            followMapper.updateFollow(followsId, userId);
            return true;
        } else if (follow != null) {
            // 取关
            remove(new LambdaQueryWrapper<Follow>()
                    .eq(Follow::getFollowId, followsId)
                    .eq(Follow::getUserId, userId));
            // 删除收件箱的视频
            // 获取关注人的视频
            Result<Collection<Long>> result = videoClient.listVideoIdByUserId(followsId);
            if (result.isFailed()) {
                throw new BaseException(result.getMessage());
            }
            List<Long> videoIds = (List<Long>) result.getData();
            feedClient.deleteInBoxFeed(userId, videoIds);
            return false;
        } else {
            // 第一次关注
            follow = new Follow();
            follow.setFollowId(followsId);
            follow.setUserId(userId);
            save(follow);
            return true;
        }
    }

    @Override
    public Boolean isFollows(Long followId, Long userId) {

        if (userId == null || followId == null) {
            return false;
        }
        Follow follow;
        follow = (Follow) redisCacheUtil.get(RedisConstant.USER_FOLLOW + userId + ":" + followId);
        if (follow == null) {
            // 查询缓存
            follow = followMapper.select(followId, userId);
        }
        if (follow == null) {
            return false;
        }
        redisCacheUtil.set(RedisConstant.USER_FOLLOW + userId + ":" + followId, follow, 600);
        return !follow.getIsDeleted();
    }
}
