package com.veeo.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.constant.RedisConstant;
import com.veeo.common.entity.user.Follow;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.exception.BaseException;


import com.veeo.common.util.RedisCacheUtil;
import com.veeo.interest.api.FeedClient;
import com.veeo.user.service.FollowService;
import com.veeo.user.mapper.FollowMapper;
import com.veeo.video.api.VideoClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;


import java.util.*;
import java.util.stream.Collectors;


@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    private final FeedClient feedClient;

    @Lazy
    private final VideoClient videoClient;

    private final RedisTemplate redisTemplate;

    private final RedisCacheUtil redisCacheUtil;

        public FollowServiceImpl(FeedClient feedClient, VideoClient videoClient, RedisTemplate redisTemplate, RedisCacheUtil redisCacheUtil) {
        this.feedClient = feedClient;
        this.videoClient = videoClient;
        this.redisTemplate = redisTemplate;
        this.redisCacheUtil = redisCacheUtil;
    }

    @Override
    public int getFollowCount(Long userId) {
        return (int) count(new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, userId));
    }

    @Override
    public int getFansCount(Long userId) {
        return (int) count(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId, userId));
    }

    @Override
    public Collection<Long> getFollow(Long userId, BasePage basePage) {
        if (basePage == null) {
            Set<Object> set = redisCacheUtil.zGet(RedisConstant.USER_FOLLOW + userId);
            if (ObjectUtils.isEmpty(set)){
                return Collections.EMPTY_SET;
            }
            return set.stream().map(o->Long.valueOf(o.toString())).collect(Collectors.toList());
        }
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisCacheUtil.zSetGetByPage(RedisConstant.USER_FOLLOW + userId, basePage.getPage(), basePage.getLimit());
        // 可能redis崩了,从db拿
        if (ObjectUtils.isEmpty(typedTuples)) {
            final List<Follow> follows = page(basePage.page(), new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, userId).orderByDesc(Follow::getGmtCreated)).getRecords();
            if (ObjectUtils.isEmpty(follows)) {
                return Collections.EMPTY_LIST;
            }
            return follows.stream().map(Follow::getFollowId).collect(Collectors.toList());
        }
        return typedTuples.stream().map(t -> Long.parseLong(t.getValue().toString())).collect(Collectors.toList());
    }




    @Override
    public Collection<Long> getFans(Long userId, BasePage basePage) {
        if (basePage == null) {
            final Set<Object> set = redisCacheUtil.zGet(RedisConstant.USER_FANS + userId);
             if(ObjectUtils.isEmpty(set)){
                return Collections.EMPTY_SET;
            }
            return set.stream().map(o->Long.valueOf(o.toString())).collect(Collectors.toList());
        }
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisCacheUtil.zSetGetByPage(RedisConstant.USER_FANS + userId, basePage.getPage(), basePage.getLimit());
        if (ObjectUtils.isEmpty(typedTuples)) {
            List<Follow> follows = page(basePage.page(),new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId, userId)).getRecords();
            if (ObjectUtils.isEmpty(follows)){
                return Collections.EMPTY_LIST;
            }
            return follows.stream().map(Follow::getUserId).collect(Collectors.toList());
        }
        return typedTuples.stream().map(t -> Long.parseLong(t.getValue().toString())).collect(Collectors.toList());
    }

    @Override
    public Boolean follows(Long followsId, Long userId) {

        if (followsId.equals(userId)) {
            throw new BaseException("你不能关注自己");
        }

        // 直接保存(唯一索引),保存失败则删除
        Follow follow = new Follow();
        follow.setFollowId(followsId);
        follow.setUserId(userId);
        try {
            save(follow);
            Date date = new Date();
            // 自己关注列表添加
            redisTemplate.opsForZSet().add(RedisConstant.USER_FOLLOW + userId, followsId, date.getTime());
            // 对方粉丝列表添加
            redisTemplate.opsForZSet().add(RedisConstant.USER_FANS + followsId, userId, date.getTime());
        } catch (Exception e) {


            // 删除
            remove(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId, followsId).eq(Follow::getUserId, userId));
            // 删除收件箱的视频
            // 获取关注人的视频
            List<Long> videoIds = (List<Long>) videoClient.listVideoIdByUserId(followsId);

            feedClient.deleteInBoxFeed(userId, videoIds);
            // 自己关注列表删除
            redisTemplate.opsForZSet().remove(RedisConstant.USER_FOLLOW + userId, followsId);
            // 对方粉丝列表删除
            redisTemplate.opsForZSet().remove(RedisConstant.USER_FANS + followsId, userId);
            return false;
        }

        return true;
    }

    @Override
    public Boolean isFollows(Long followId, Long userId) {
        if (userId == null || followId == null) return false;
        return count(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId,followId).eq(Follow::getUserId,userId)) == 1;
    }
}
