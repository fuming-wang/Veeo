package com.veeo.video.handler.video;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.video.config.BizContext;
import com.veeo.video.constant.AuditStatus;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName ListByUserIdOpenVideoId
 * @Description
 * @Author wangfuming
 * @Date 2025/7/26 13:05
 * @Version 1.0.0
 */
@Service
public class ListByUserIdOpenVideoHandler implements BizHandler {

    @Resource
    private VideoService videoService;

    @Override
    public void handle(BizContext context) {
        Long userId = context.getUserId();
        BasePage basePage = context.getBasePage();
        IPage<Video> page = videoService.page(basePage.videoPage(),
                new LambdaQueryWrapper<Video>()
                        .eq(Video::getUserId, userId)
                        .eq(Video::getAuditStatus, AuditStatus.SUCCESS)
                        .orderByDesc(Video::getGmtCreated));
        List<Video> videos = page.getRecords();
        context.setVideoPage(page);
        context.setVideos(videos);
    }
}
