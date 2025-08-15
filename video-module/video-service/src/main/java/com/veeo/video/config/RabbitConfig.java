package com.veeo.video.config;

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

    public static final String VIDEO_FANOUT_EXCHANGE = "videoFanoutExchange";

    public static final String REDIS_VIDEO_QUEUE = "redis.video.queue";

    @Bean
    public FanoutExchange videoFanoutExchange() {
        return new FanoutExchange(VIDEO_FANOUT_EXCHANGE);
    }

    @Bean
    public Queue redisVideoQueue() {
        return new Queue(REDIS_VIDEO_QUEUE);
    }

    @Bean
    public Binding bindingA(Queue redisVideoQueue, FanoutExchange exchange) {
        return BindingBuilder.bind(redisVideoQueue).to(exchange);
    }
}
