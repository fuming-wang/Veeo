package com.veeo.video.handler.video;

import com.veeo.common.entity.video.Video;
import com.veeo.interest.client.FeedClient;
import com.veeo.interest.client.InterestClient;
import com.veeo.video.config.BizContext;
import com.veeo.video.constant.AuditStatus;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @ClassName AuditVideoHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/24 22:44
 * @Version 1.0.0
 */
@Service
public class AuditVideoHandler implements BizHandler {

    @Resource
    private VideoService videoService;

    @Resource
    private InterestClient interestClient;

    @Resource
    private FeedClient feedClient;

    @Override
    public void handle(BizContext context) {
        Video video = context.getVideo();
        // 由审核员手动审核
        video.setAuditStatus(AuditStatus.SUCCESS);
        video.setMsg("通过");
        videoService.updateById(video);
        interestClient.pushSystemStockIn(video);
        interestClient.pushSystemTypeStockIn(video);
        // 推送该视频博主的发件箱
        feedClient.pushOutBoxFeed(video.getUserId(), video.getId(), video.getGmtCreated().getTime());
    }
}
