package com.veeo.video.handler.video;

import com.veeo.video.config.BizContext;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.FavoritesService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;

/**
 * @ClassName GetVideoIdsByFavoritesHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/25 22:19
 * @Version 1.0.0
 */
@Service
public class GetVideoIdsByFavoritesHandler implements BizHandler {

    @Resource
    private FavoritesService favoritesService;

    @Override
    public void handle(BizContext context) {
        Long userId = context.getUserId();
        Long favoritesId = context.getFavoriteId();
        List<Long> videoIds = favoritesService.listVideoIds(favoritesId, userId);
        if (ObjectUtils.isEmpty(videoIds)) {
            context.setVideoIds(Collections.emptyList());
        }
        context.setVideoIds(videoIds);
    }
}
