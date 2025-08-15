package com.veeo.video.handler.video;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veeo.common.entity.video.Video;
import com.veeo.common.exception.BaseException;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.video.config.BizContext;
import com.veeo.video.constant.RedisConstant;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @ClassName GetVideoByIdHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/24 21:59
 * @Version 1.0.0
 */
@Service
public class GetVideoByIdHandler implements BizHandler {

    @Resource
    private VideoService videoService;

    @Resource
    private RedisCacheUtil redisCacheUtil;

    @Override
    public void handle(BizContext context) {
        Long videoId = context.getVideoId();
        Video video;
        // 先查缓存
        video = (Video) redisCacheUtil.get(RedisConstant.VIDEO_CACHE + videoId);
        // 缓存没有命中
        if (ObjectUtils.isEmpty(video)) {
            video = videoService.getById(videoId);
            // 即使不存在也要构造缓存, 如果是未来的id, 在加入时清理key
            redisCacheUtil.set(RedisConstant.VIDEO_CACHE + videoId, video, 600);
        }
        // 数据库中没有视频
        if (video == null) {
            throw new BaseException("指定视频不存在");
        }
        if (video.getOpen()) {
            context.setVideo(new Video());
            return;
        }
        context.setVideo(video);
    }
}
