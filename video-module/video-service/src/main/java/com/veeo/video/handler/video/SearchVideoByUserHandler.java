package com.veeo.video.handler.video;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.user.client.UserClient;
import com.veeo.video.config.BizContext;
import com.veeo.video.constant.AuditStatus;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * @ClassName SearchVideoByUserHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/25 21:45
 * @Version 1.0.0
 */
@Service
public class SearchVideoByUserHandler implements BizHandler {

    @Resource
    private VideoService videoService;

    @Resource
    private UserClient userClient;

    @Override
    public void handle(BizContext context) {
        String search = context.getSearch();
        BasePage basePage = context.getBasePage();
        Long userId = context.getUserId();


        // 如果带YV则精准搜该视频
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getAuditStatus, AuditStatus.SUCCESS);
        if (search.contains("YV")) {
            wrapper.eq(Video::getYv, search);
        } else {
            wrapper.like(!ObjectUtils.isEmpty(search), Video::getTitle, search);
        }
        IPage<Video> page = videoService.page(basePage.videoPage(), wrapper);
        List<Video> videos = page.getRecords();
        userClient.addSearchHistory(userId, search);
        context.setVideos(videos);
        context.setVideoPage(page);
    }
}
