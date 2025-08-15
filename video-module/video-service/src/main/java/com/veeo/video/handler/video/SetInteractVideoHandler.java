package com.veeo.video.handler.video;

import com.veeo.common.entity.video.Video;
import com.veeo.user.client.FollowClient;
import com.veeo.video.config.BizContext;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.FavoritesService;
import com.veeo.video.service.VideoStarService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @ClassName SetInteractVideoHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/24 22:14
 * @Version 1.0.0
 */
@Service
public class SetInteractVideoHandler implements BizHandler {

    @Resource
    private VideoStarService videoStarService;

    @Resource
    private FavoritesService favoritesService;

    @Resource
    private FollowClient followClient;

    @Override
    public void handle(BizContext context) {
        Long userId = context.getUserId();
        if (userId == null) {
            return;
        }
        Video video = context.getVideo();
        Long videoId = video.getId();
        video.setStart(videoStarService.starState(videoId, userId)); // 视频有没有点赞
        video.setFavorites(favoritesService.favoritesState(videoId, userId)); // 视频有没有收藏
        video.setFollow(followClient.isFollows(video.getUserId(), userId).getData()); // 有没有关注
    }
}
