package com.veeo.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.veeo.common.entity.user.FavoritesVideo;
import com.veeo.common.util.Result;


public interface FavoritesVideoService extends IService<FavoritesVideo> {
    Result<Boolean> removeFavoritesVideo(Long favoritesId);
}
