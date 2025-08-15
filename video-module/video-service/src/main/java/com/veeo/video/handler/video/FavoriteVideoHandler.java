package com.veeo.video.handler.video;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.UserModel;
import com.veeo.interest.client.InterestClient;
import com.veeo.video.config.BizContext;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.FavoritesService;
import com.veeo.video.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName FavoritiesVideoHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/25 20:49
 * @Version 1.0.0
 */
@Service
public class FavoriteVideoHandler implements BizHandler {

    @Resource
    private FavoritesService favoritesService;

    @Resource
    private InterestClient interestClient;

    @Resource
    private VideoService videoService;

    @Override
    public void handle(BizContext context) {

        Video video = context.getVideo();
        Long favoritesId = context.getFavoriteId();
        Long videoId = context.getVideoId();
        Long userId = context.getUserId();

        boolean favoritesResult = favoritesService.favorites(favoritesId, videoId, userId);

        updateFavorites(video, favoritesResult ? 1L : -1L);

        List<String> labels = video.buildLabel();

        UserModel userModel = UserModel.buildUserModel(labels, videoId, 2.0);
        interestClient.updateUserModel(userModel);
        context.setResult(favoritesResult);
    }

    public void updateFavorites(Video video, Long value) {
        UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("favorites_count = favorites_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getFavoritesCount, video.getFavoritesCount());
        videoService.update(video, updateWrapper);
    }
}
