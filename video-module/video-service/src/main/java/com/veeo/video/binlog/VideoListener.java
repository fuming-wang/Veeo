package com.veeo.video.binlog;

import com.gitee.Jmysy.binlog4j.core.BinlogEvent;
import com.gitee.Jmysy.binlog4j.core.IBinlogEventHandler;
import com.gitee.Jmysy.binlog4j.springboot.starter.annotation.BinlogSubscriber;
import com.veeo.common.entity.video.Video;
import com.veeo.video.config.RabbitConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * @ClassName VideoListener
 * @Description
 * @Author wangfuming
 * @Date 2025/7/28 21:55
 * @Version 1.0.0
 */
@Slf4j
@BinlogSubscriber(clientName = "master")
public class VideoListener implements IBinlogEventHandler<Video> {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public void onInsert(BinlogEvent<Video> binlogEvent) {
        // 不用管
    }

    @Override
    public void onUpdate(BinlogEvent<Video> binlogEvent) {
        Video video = binlogEvent.getData();
        log.warn("[videoUpdate]: {}", video);
        rabbitTemplate.convertAndSend(RabbitConfig.REDIS_VIDEO_QUEUE, "", video);
    }

    @Override
    public void onDelete(BinlogEvent<Video> binlogEvent) {
        Video video = binlogEvent.getOriginalData();
        log.warn("[videoDelete]: {}", video);
        rabbitTemplate.convertAndSend(RabbitConfig.REDIS_VIDEO_QUEUE, "", video);
    }

    @Override
    public boolean isHandle(String s, String s1) {
        return "veeo_video".equals(s) && "video".equals(s1);
    }
}
