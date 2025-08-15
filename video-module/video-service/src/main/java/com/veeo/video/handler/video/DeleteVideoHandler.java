package com.veeo.video.handler.video;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.video.VideoShare;
import com.veeo.common.entity.video.VideoStar;
import com.veeo.common.exception.BaseException;
import com.veeo.interest.client.InterestClient;
import com.veeo.video.config.BizContext;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.VideoService;
import com.veeo.video.service.VideoShareService;
import com.veeo.video.service.VideoStarService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

/**
 * @ClassName DeleteVideoHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/24 22:28
 * @Version 1.0.0
 */
@Service
public class DeleteVideoHandler implements BizHandler {

    @Resource
    private VideoService videoService;

    @Resource
    private VideoStarService videoStarService;

    @Resource
    private VideoShareService videoShareService;

    @Resource
    private InterestClient interestClient;

    @Resource
    private ExecutorService executorService;

    @Override
    public void handle(BizContext context) {

        Long userId = context.getUserId();
        Long videoId = context.getVideoId();
        // todo 继续下沉
        Video video = videoService.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, videoId).eq(Video::getUserId, userId));
        if (video == null) {
            throw new BaseException("删除指定的视频不存在");
        }
        boolean b = videoService.removeById(videoId);

        if (b) {
            // 解耦
            executorService.submit(() -> {
                // 删除分享量 点赞量
                // todo 继续下沉
                videoShareService.remove(new LambdaQueryWrapper<VideoShare>().eq(VideoShare::getVideoId, videoId).eq(VideoShare::getUserId, userId));
                videoStarService.remove(new LambdaQueryWrapper<VideoStar>().eq(VideoStar::getVideoId, videoId).eq(VideoStar::getUserId, userId));
                interestClient.deleteSystemStockIn(video);
                interestClient.deleteSystemTypeStockIn(video);
            });
        }
    }
}
