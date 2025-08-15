package com.veeo.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.veeo.common.entity.user.Favorites;
import com.veeo.common.util.Result;

import java.util.List;

public interface FavoritesService extends IService<Favorites> {

    /**
     * 删除收藏夹,连收藏夹下的视频一块删除
     */
    Result<Boolean> remove(Long favoriteId, Long userId);

    /**
     * 根据用户获取收藏夹
     */
    List<Favorites> listByUserId(Long userId);

    /**
     * 获取收藏夹下的所有视频id
     */
    List<Long> listVideoIds(Long favoritesId, Long userId);

    /**
     * 收藏视频
     *
     */
    boolean favorites(Long favoritesId, Long videoId, Long userId);

    /**
     * 收藏状态
     */
    Boolean favoritesState(Long videoId, Long userId);


    void exist(Long userId, Long defaultFavoritesId);
}
