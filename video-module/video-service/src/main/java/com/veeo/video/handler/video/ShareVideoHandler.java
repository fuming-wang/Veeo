package com.veeo.video.handler.video;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.video.VideoShare;
import com.veeo.video.config.BizContext;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.VideoService;
import com.veeo.video.service.VideoShareService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @ClassName ShareVideoHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/25 21:25
 * @Version 1.0.0
 */
@Service
public class ShareVideoHandler implements BizHandler {

    @Resource
    private VideoService videoService;

    @Resource
    private VideoShareService videoShareService;

    @Override
    public void handle(BizContext context) {
        Video video = context.getVideo();
        VideoShare videoShare = context.getVideoShare();
        boolean result = videoShareService.share(videoShare);
        updateShare(video, result ? 1L : 0L);
    }

    public void updateShare(Video video, Long value) {
        UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("share_count = share_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getShareCount, video.getShareCount());
        videoService.update(video, updateWrapper);
    }
}
