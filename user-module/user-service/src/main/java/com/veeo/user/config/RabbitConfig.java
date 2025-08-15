package com.veeo.user.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName RabbitConfig
 * @Description
 * @Author wangfuming
 * @Date 2025/7/28 21:48
 * @Version 1.0.0
 */
@Configuration
public class RabbitConfig {

    public static final String USER_FANOUT_EXCHANGE = "userFanoutExchange";

    public static final String REDIS_USER_QUEUE = "redis.user.queue";

    public static final String FOLLOW_FANOUT_EXCHANGE = "followFanoutExchange";

    public static final String REDIS_FOLLOW_QUEUE = "redis.follow.queue";

    @Bean
    public FanoutExchange userFanoutExchange() {
        return new FanoutExchange(USER_FANOUT_EXCHANGE);
    }
    @Bean
    public FanoutExchange followFanoutExchange() {
        return new FanoutExchange(FOLLOW_FANOUT_EXCHANGE);
    }

    @Bean
    public Queue redisUserQueue() {
        return new Queue(REDIS_USER_QUEUE);
    }

    @Bean
    public Queue redisFollowQueue() {
        return new Queue(REDIS_FOLLOW_QUEUE);
    }

    @Bean
    public Binding bindingUser(Queue redisUserQueue, FanoutExchange userFanoutExchange) {
        return BindingBuilder.bind(redisUserQueue).to(userFanoutExchange);
    }

    @Bean
    public Binding bindingFollow(Queue redisFollowQueue, FanoutExchange followFanoutExchange) {
        return BindingBuilder.bind(redisFollowQueue).to(followFanoutExchange);
    }
}
