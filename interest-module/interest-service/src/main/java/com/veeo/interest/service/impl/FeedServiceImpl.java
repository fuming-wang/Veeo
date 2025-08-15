package com.veeo.interest.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeo.common.util.DateUtil;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.interest.config.RedisConstant;
import com.veeo.interest.service.FeedService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Slf4j
@Service
public class FeedServiceImpl implements FeedService {


    @Resource
    private RedisCacheUtil redisCacheUtil;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    private final ObjectMapper objectMapper = new ObjectMapper();



    /**
     * 推入发件箱
     *
     * @param userId  发件箱用户id
     * @param videoId 视频id
     * @param time    时间
     */
    @Override
    @Async
    public void pushOutBoxFeed(Long userId, Long videoId, Long time) {
        redisCacheUtil.zadd(RedisConstant.OUT_FOLLOW + userId, time, videoId, -1);
    }

    /**
     * 推入收件箱
     *
     * @param userId  收件箱用户id
     * @param videoId 视频id
     * @param time    时间
     */
    @Override
    public void pushInBoxFeed(Long userId, Long videoId, Long time) {
        log.error("错误的请求");
    }


    /**
     * 删除发件箱
     * 当前用户删除视频时调用, 删除当前用户的发件箱中视频以及粉丝下的收件箱
     *
     * @param userId  当前用户
     * @param fans    粉丝id
     * @param videoId 视频id 需要删除的
     */
    @Override
    @Async
    public void deleteOutBoxFeed(Long userId, Collection<Long> fans, Long videoId) {
        String t = RedisConstant.IN_FOLLOW;
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            // 删除粉丝的收件箱
            for (Long fan : fans) {
                connection.zSetCommands().zRem((t + fan).getBytes(), String.valueOf(videoId).getBytes());
            }
            // 删除自己的发件箱
            connection.zSetCommands().zRem((RedisConstant.OUT_FOLLOW + userId).getBytes(), String.valueOf(videoId).getBytes());
            return null;
        });
    }

    /**
     * 删除收件箱
     * 当前用户取关用户时调用
     * 删除自己收件箱中的videoIds
     *
     * @param userId   用户id
     * @param videoIds 关注人发的视频id
     */
    @Override
    @Async
    public void deleteInBoxFeed(Long userId, List<Long> videoIds) {
        redisTemplate.opsForZSet().remove(RedisConstant.IN_FOLLOW + userId, videoIds.toArray());
    }

    /**
     * 初始化关注流-拉模式 with TTL
     *
     * @param userId 用户id
     */
    @Override
    @Async
    public void initFollowFeed(Long userId, Collection<Long> followIds) {
        String t2 = RedisConstant.IN_FOLLOW;
        Date curDate = new Date();
        Date limitDate = DateUtil.addDateDays(curDate, -7);

        Set<ZSetOperations.TypedTuple<Object>> set = redisTemplate.opsForZSet().rangeWithScores(t2 + userId, -1, -1);
        if (!ObjectUtils.isEmpty(set)) {
            Double oldTime = set.iterator().next().getScore();
            init(userId, oldTime.longValue(), new Date().getTime(), followIds);
        } else {
            init(userId, limitDate.getTime(), curDate.getTime(), followIds);
        }

    }

    public void init(Long userId, Long min, Long max, Collection<Long> followIds) {
        String t1 = RedisConstant.OUT_FOLLOW;
        String t2 = RedisConstant.IN_FOLLOW;
        // 查看关注人的发件箱
        List<Object> result = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long followId : followIds) {
                connection.zRevRangeByScoreWithScores((t1 + followId).getBytes(), min, max, 0, 50);
            }
            return null;
        });

        Set<Long> ids = new HashSet<>();
        // 放入收件箱
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Object tuples : result) {
                Set<DefaultTypedTuple<Long>> tuples1 = (Set<DefaultTypedTuple<Long>>) tuples;
                if (ObjectUtils.isEmpty(tuples1)) {
                    continue;
                }
                tuples1.forEach(tuple -> {
                    Object value = tuple.getValue();
                    ids.add(Long.parseLong(value.toString()));
                    byte[] key = (t2 + userId).getBytes();
                    try {
                        connection.zAdd(key, tuple.getScore(), objectMapper.writeValueAsBytes(value));
                    } catch (JsonProcessingException e) {
                        log.error("init error {}", e.getMessage());
                    }
                    connection.keyCommands().expire(key, RedisConstant.HISTORY_TIME);
                });
            }
            return null;
        });
    }

}
