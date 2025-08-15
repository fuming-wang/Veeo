package com.veeo.video.handler.video;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.video.VideoStar;
import com.veeo.common.entity.vo.UserModel;
import com.veeo.common.holder.UserHolder;
import com.veeo.interest.client.InterestClient;
import com.veeo.video.config.BizContext;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.VideoService;
import com.veeo.video.service.VideoStarService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName StarVideoHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/24 22:54
 * @Version 1.0.0
 */
@Service
public class StarVideoHandler implements BizHandler {

    @Resource
    private VideoService videoService;

    @Resource
    private VideoStarService videoStarService;

    @Resource
    private InterestClient interestClient;

    @Override
    public void handle(BizContext context) {
        Video video = context.getVideo();
        Long videoId = video.getId();
        VideoStar videoStar = new VideoStar();
        videoStar.setVideoId(videoId);
        videoStar.setUserId(UserHolder.get());
        boolean result = videoStarService.starVideo(videoStar);
        updateStar(video, result ? 1L : -1L);
        // 获取标签
        List<String> labels = video.buildLabel();

        UserModel userModel = UserModel.buildUserModel(labels, videoId, 1.0);
        interestClient.updateUserModel(userModel);
        context.setResult(true);
    }

    public void updateStar(Video video, Long value) {
        UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("start_count = start_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getStartCount, video.getStartCount());

        videoService.update(video, updateWrapper);

    }
}
