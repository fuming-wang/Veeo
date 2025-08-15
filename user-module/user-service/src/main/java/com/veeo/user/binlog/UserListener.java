package com.veeo.user.binlog;

import com.gitee.Jmysy.binlog4j.core.BinlogEvent;
import com.gitee.Jmysy.binlog4j.core.IBinlogEventHandler;
import com.gitee.Jmysy.binlog4j.springboot.starter.annotation.BinlogSubscriber;
import com.veeo.common.entity.user.User;
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
public class UserListener implements IBinlogEventHandler<User> {


    @Resource
    private RabbitTemplate rabbitTemplate;


    @Override
    public void onInsert(BinlogEvent<User> binlogEvent) {
        // 不用管
    }

    @Override
    public void onUpdate(BinlogEvent<User> binlogEvent) {
        User user = binlogEvent.getData();
        rabbitTemplate.convertAndSend(RabbitConfig.USER_FANOUT_EXCHANGE, "", user);
    }

    @Override
    public void onDelete(BinlogEvent<User> binlogEvent) {
        User user = binlogEvent.getOriginalData();
        rabbitTemplate.convertAndSend(RabbitConfig.USER_FANOUT_EXCHANGE, "", user);
    }

    @Override
    public boolean isHandle(String s, String s1) {
        return "veeo_user".equals(s) && "user".equals(s1);
    }
}
