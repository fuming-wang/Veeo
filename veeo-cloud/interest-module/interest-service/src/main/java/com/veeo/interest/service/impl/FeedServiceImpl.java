package com.veeo.interest.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeo.common.constant.RedisConstant;
import com.veeo.common.util.DateUtil;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.interest.service.FeedService;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;


import java.util.*;


@Service
public class FeedServiceImpl implements FeedService {


    private final RedisCacheUtil redisCacheUtil;

    private final RedisTemplate redisTemplate;

    public FeedServiceImpl(RedisCacheUtil redisCacheUtil, RedisTemplate redisTemplate) {
        this.redisCacheUtil = redisCacheUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Async
    public void pushOutBoxFeed(Long userId, Long videoId, Long time) {
        redisCacheUtil.zadd(RedisConstant.OUT_FOLLOW + userId, time, videoId, -1);
    }

    @Override
    public void pushInBoxFeed(Long userId, Long videoId, Long time) {
        // 需要推吗这个场景？只需要拉
    }

    @Override
    @Async
    public void deleteOutBoxFeed(Long userId, Collection<Long> fans, Long videoId) {
        String t = RedisConstant.IN_FOLLOW;
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long fan : fans) {
                connection.zRem((t+fan).getBytes(),String.valueOf(videoId).getBytes());
            }
            connection.zRem((RedisConstant.OUT_FOLLOW + userId).getBytes(), String.valueOf(videoId).getBytes());
            return null;
        });
    }

    @Override
    @Async
    public void deleteInBoxFeed(Long userId, List<Long> videoIds) {
        redisTemplate.opsForZSet().remove(RedisConstant.IN_FOLLOW + userId, videoIds.toArray());
    }


    @Override
    @Async
    public void initFollowFeed(Long userId, Collection<Long> followIds) {
        String t2 = RedisConstant.IN_FOLLOW;
        Date curDate = new Date();
        Date limitDate = DateUtil.addDateDays(curDate, -7);

        Set<ZSetOperations.TypedTuple<Long>> set = redisTemplate.opsForZSet().rangeWithScores(t2 + userId, -1, -1);
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
        List<Set<DefaultTypedTuple>> result = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (Long followId : followIds) {
                    connection.zRevRangeByScoreWithScores((t1 + followId).getBytes(), min, max, 0, 50);
                }
                return null;
            });
        ObjectMapper objectMapper = new ObjectMapper();
        HashSet<Long> ids = new HashSet<>();
        // 放入收件箱
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Set<DefaultTypedTuple> tuples : result) {
                if (!ObjectUtils.isEmpty(tuples)) {

                    for (DefaultTypedTuple tuple : tuples) {

                        Object value = tuple.getValue();
                        ids.add(Long.parseLong(value.toString()));
                        byte[] key = (t2 + userId).getBytes();
                        try {
                            connection.zAdd(key, tuple.getScore(), objectMapper.writeValueAsBytes(value));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        connection.expire(key, RedisConstant.HISTORY_TIME);
                    }
                }
            }
            return null;
        });
    }

}
