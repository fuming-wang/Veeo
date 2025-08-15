package com.veeo.user.redis;

import com.veeo.common.entity.user.Follow;
import com.veeo.common.entity.user.User;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.user.config.RabbitConfig;
import com.veeo.user.constant.RedisConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * @ClassName RedisDeleteKey
 * @Description
 * @Author wangfuming
 * @Date 2025/7/28 22:20
 * @Version 1.0.0
 */
@Slf4j
@Component
public class RedisDeleteKey {

    @Resource
    private RedisCacheUtil redisCacheUtil;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.REDIS_FOLLOW_QUEUE)
    public void deleteFollow(Follow follow) {
        Long userId = follow.getUserId();
        Long followsId = follow.getFollowId();
        try {
            // 删除粉丝/关注数缓存
            redisCacheUtil.del(RedisConstant.USER_FOLLOW_NUMBER_CACHE + userId);
            redisCacheUtil.del(RedisConstant.USER_FANS_NUMBER_CACHE + followsId);
            // 删除粉丝/关注人列表缓存
            redisCacheUtil.del(RedisConstant.USER_FOLLOW + userId);
            redisCacheUtil.del(RedisConstant.USER_FANS + followsId);
        } catch (Exception e) {
            log.error("[RedisDeleteKey#deleteFollow] get error: {}", e.getMessage());
            rabbitTemplate.convertAndSend(RabbitConfig.FOLLOW_FANOUT_EXCHANGE, "", follow);
        }
    }

    @RabbitListener(queues = RabbitConfig.REDIS_USER_QUEUE)
    public void deleteUser(User user) {
        try {
            // 删除粉丝/关注数缓存
            redisCacheUtil.del(RedisConstant.USER_CACHE + user.getId());
        } catch (Exception e) {
            log.error("[RedisDeleteKey#deleteUser] get error: {}", e.getMessage());
            rabbitTemplate.convertAndSend(RabbitConfig.USER_FANOUT_EXCHANGE, "", user);
        }
    }
}
