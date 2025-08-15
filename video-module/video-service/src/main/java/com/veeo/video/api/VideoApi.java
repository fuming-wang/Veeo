package com.veeo.video.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.video.biz.VideoBiz;
import com.veeo.video.client.VideoClient;
import com.veeo.video.config.BizContext;
import com.veeo.video.service.VideoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/api/video")
public class VideoApi implements VideoClient {

    @Resource
    private VideoService videoService;

    @Resource
    private VideoBiz videoBiz;

    @Override
    public Result<IPage<Video>> listByUserIdVideo(BasePage basePage, Long userId) {
        BizContext context = BizContext.create();
        context.setUserId(userId);
        context.setBasePage(basePage);
        videoBiz.listByUserIdVideo(context);

        return ResultUtil.getSucRet(context.getVideoPage());
    }

    @Override
    public Result<Collection<Long>> listVideoIdByUserId(Long userId) {

        return ResultUtil.getSucRet(videoService.listVideoIdByUserId(userId));
    }
}
