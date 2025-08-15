package com.veeo.video.handler.video;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.video.config.BizContext;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @ClassName listByUserIdVideoHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/26 14:03
 * @Version 1.0.0
 */
@Service
public class ListByUserIdVideoHandler implements BizHandler {

    @Resource
    private VideoService videoService;

    @Override
    public void handle(BizContext context) {
        BasePage basePage = new BasePage();
        Long userId = context.getUserId();
        IPage page = videoService.page(basePage.page(), new LambdaQueryWrapper<Video>().eq(Video::getUserId, userId).orderByDesc(Video::getGmtCreated));
        context.setVideoIds(page.getRecords());
    }
}
