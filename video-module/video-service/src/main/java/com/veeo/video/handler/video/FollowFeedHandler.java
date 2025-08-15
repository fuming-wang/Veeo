package com.veeo.video.handler.video;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veeo.common.entity.video.Video;
import com.veeo.video.config.BizContext;
import com.veeo.video.constant.RedisConstant;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * @ClassName FollowFeedHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/26 14:37
 * @Version 1.0.0
 */
@Service
public class FollowFeedHandler implements BizHandler {

    @Resource
    private VideoService videoService;

    @Resource
    private RedisTemplate<String, Long> redisTemplate;

    @Override
    public void handle(BizContext context) {
        Long userId = context.getUserId();
        Long lastTime = context.getLastTime();

        // 我们以时间戳作为分数，分页通常是基于上一次返回的最后一个元素的时间戳来做条件的，如果不跳过上一次的边界值，就会出现重复数据。
        Set<Long> set = redisTemplate.opsForZSet().reverseRangeByScore(RedisConstant.IN_FOLLOW + userId,
                0, lastTime == null ? new Date().getTime() : lastTime, lastTime == null ? 0 : 1, 5);

        if (ObjectUtils.isEmpty(set)) {
            // 可能只是缓存中没有了,缓存只存储7天内的关注视频,继续往后查看关注的用户太少了,不做考虑 - feed流必然会产生的问题
            context.setVideoIds(Collections.emptyList());
        }

        // 这里不会按照时间排序，需要手动排序
        Collection<Video> videos = videoService.list(new LambdaQueryWrapper<Video>().in(Video::getId, set).orderByDesc(Video::getGmtCreated));

        context.setVideos(videos);

    }
}
