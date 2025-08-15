package com.veeo.user.binlog;

import com.gitee.Jmysy.binlog4j.core.BinlogEvent;
import com.gitee.Jmysy.binlog4j.core.IBinlogEventHandler;
import com.gitee.Jmysy.binlog4j.springboot.starter.annotation.BinlogSubscriber;
import com.veeo.common.entity.user.Follow;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.user.config.RabbitConfig;
import com.veeo.user.constant.RedisConstant;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * @ClassName UserBinLogListenerRedisProcessor
 * @Description
 * @Author wangfuming
 * @Date 2025/7/5 13:51
 * @Version 1.0.0
 */
@BinlogSubscriber(clientName = "master")
public class FollowListener implements IBinlogEventHandler<Follow> {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public void onInsert(BinlogEvent<Follow> binlogEvent) {
        rabbitTemplate.convertAndSend(RabbitConfig.FOLLOW_FANOUT_EXCHANGE, "", binlogEvent.getData());
    }

    @Override
    public void onUpdate(BinlogEvent<Follow> binlogEvent) {
        rabbitTemplate.convertAndSend(RabbitConfig.FOLLOW_FANOUT_EXCHANGE, "", binlogEvent.getData());
    }

    @Override
    public void onDelete(BinlogEvent<Follow> binlogEvent) {
        rabbitTemplate.convertAndSend(RabbitConfig.FOLLOW_FANOUT_EXCHANGE, "", binlogEvent.getData());
    }

    @Override
    public boolean isHandle(String s, String s1) {
        return "veeo_user".equals(s) && "follow".equals(s1);
    }
}
