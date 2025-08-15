package com.veeo.video.redis;


import com.veeo.common.entity.video.Video;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.video.config.RabbitConfig;
import com.veeo.video.constant.RedisConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


/**
 * @ClassName VideoBinLogListenerRedisProcessor
 * @Description
 * @Author wangfuming
 * @Date 2025/7/4 23:31
 * @Version 1.0.0
 */
@Component
@Slf4j
public class RedisDeleteKey {

    @Resource
    private RedisCacheUtil redisCacheUtil;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.REDIS_VIDEO_QUEUE)
    public void deleteVideo(Video video) {
        log.warn("redis cache delete video {}", video);
        try {
            redisCacheUtil.del(RedisConstant.VIDEO_CACHE + video.getId());
        } catch (Exception e) {
            log.error("[RedisDeleteKey#deleteVideo] get error: {}", e.getMessage());
            rabbitTemplate.convertAndSend(RabbitConfig.REDIS_VIDEO_QUEUE, "", video);
        }
    }
}
