package com.veeo.video.handler.video;

import com.veeo.common.entity.video.Video;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.video.config.BizContext;
import com.veeo.video.constant.RedisConstant;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @ClassName GetVideoByIdsHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/25 21:04
 * @Version 1.0.0
 */
@Service
public class GetVideoByIdsHandler implements BizHandler {


    @Resource
    private VideoService videoService;

    @Resource
    private RedisCacheUtil redisCacheUtil;

    @Override
    public void handle(BizContext context) {
        Collection<Long> ids = context.getVideoIds();
        List<Video> videos = new ArrayList<>(ids.size());
        List<Long> videoIds = new ArrayList<>();
        ids.forEach(id -> {
            Video video = (Video) redisCacheUtil.get(RedisConstant.VIDEO_CACHE + id);
            if (ObjectUtils.isEmpty(video)) {
                videoIds.add(id);
            } else {
                videos.add(video);
            }
        });
        List<Video> videoList = videoService.listByIds(videoIds);
        // todo 异步将视频加入缓存
        videos.addAll(videoList);
        context.setVideos(videos);
    }
}
