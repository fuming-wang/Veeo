package com.veeo.video.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.video.api.VideoClient;
import com.veeo.video.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/api/video")
public class APIVideoController implements VideoClient {

    private final VideoService videoService;

    public APIVideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @Override
    public IPage<Video> listByUserIdVideo(BasePage basePage, Long userId) {
        if (basePage == null || userId == null) {
            log.error("basePage or userId is null");
        }
        return videoService.listByUserIdVideo(basePage, userId);
    }

    @Override
    public Collection<Long> listVideoIdByUserId(Long userId) {
        if (userId == null) {
            log.error("userId is null");
        }
        return videoService.listVideoIdByUserId(userId);
    }
}
