package com.veeo.video.biz;

import com.veeo.common.entity.user.Favorites;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.video.constant.ErrorMsg;
import com.veeo.video.service.FavoritesService;
import com.veeo.video.service.FavoritesVideoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutorService;

/**
 * @ClassName FavotiteBiz
 * @Description
 * @Author wangfuming
 * @Date 2025/7/28 21:02
 * @Version 1.0.0
 */
@Service
public class FavoriteBiz {

    private static final String DEFAULT_FAVORITE = "默认收藏夹";

    @Resource
    private FavoritesService favoritesService;

    @Resource
    private FavoritesVideoService favoritesVideoService;

    @Resource
    private ExecutorService executorService;

    @Transactional
    public Result<Boolean> deleteFavorites(Long favoritesId, Long userId) {
        Favorites favorites = favoritesService.getById(favoritesId);
        // 不能删除默认收藏夹
        if (DEFAULT_FAVORITE.equals(favorites.getName())) {
            return ResultUtil.getFailRet(ErrorMsg.DEFAULT_FAVORITES_NOT_DELETE);
        }
        Result<Boolean> deleteResult = favoritesService.remove(favoritesId, userId);
        if (deleteResult.isFailed() || !deleteResult.getData()) {
            return ResultUtil.getFailRet(ErrorMsg.DEFAULT_FAVORITES_NOT_DELETE);
        }
        executorService.execute(() -> {
            Result<Boolean> tmp = favoritesVideoService.removeFavoritesVideo(favoritesId);
        });

        return ResultUtil.getSucRet(Boolean.TRUE);
    }

}
