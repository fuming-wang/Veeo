package com.veeo.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.entity.user.Favorites;
import com.veeo.common.entity.user.FavoritesVideo;
import com.veeo.common.exception.BaseException;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.video.mapper.FavoritesMapper;
import com.veeo.video.service.FavoritesService;
import com.veeo.video.service.FavoritesVideoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, Favorites> implements FavoritesService {


    @Resource
    private FavoritesVideoService favoritesVideoService;


    @Override
    public Result<Boolean> remove(Long favoriteId, Long userId) {
        boolean result;
        try {
            result = remove(new LambdaQueryWrapper<Favorites>()
                    .eq(Favorites::getId, favoriteId)
                    .eq(Favorites::getUserId, userId));
        } catch (Exception e) {
            log.error("[FavoritesServiceImpl] remove error", e);
            return ResultUtil.getFailRet(e.getMessage());
        }
        return ResultUtil.getSucRet(result);
    }

    @Override
    public List<Favorites> listByUserId(Long userId) {
        // 查出收藏夹id
        List<Favorites> favorites = list(new LambdaQueryWrapper<Favorites>().eq(Favorites::getUserId, userId));
        if (ObjectUtils.isEmpty(favorites)) {
            return Collections.emptyList();
        }
        // 根据收藏夹id获取对应数
        List<Long> fIds = favorites.stream().map(Favorites::getId).collect(Collectors.toList());
        Map<Long, Long> fMap = favoritesVideoService.list(new LambdaQueryWrapper<FavoritesVideo>().in(FavoritesVideo::getFavoritesId, fIds))
                .stream().collect(Collectors.groupingBy(FavoritesVideo::getFavoritesId, Collectors.counting()));
        // 计算对应视频总数
        favorites.forEach(favorite -> {
            Long videoCount = fMap.get(favorite.getId());
            favorite.setVideoCount(videoCount == null ? 0 : videoCount);
        });
        return favorites;
    }

    @Override
    public List<Long> listVideoIds(Long favoritesId, Long userId) {

        // 不直接返回中间表是为了隐私性 (当前没实现收藏夹公开功能)
        // 校验
        Favorites favorites = getOne(new LambdaQueryWrapper<Favorites>().eq(Favorites::getId, favoritesId).eq(Favorites::getUserId, userId));
        if (favorites == null) {
            throw new BaseException("收藏夹为空");
        }

        return favoritesVideoService.list(new LambdaQueryWrapper<FavoritesVideo>().eq(FavoritesVideo::getFavoritesId, favoritesId))
                .stream().map(FavoritesVideo::getVideoId).collect(Collectors.toList());
    }

    @Override
    public boolean favorites(Long favoritesId, Long videoId, Long userId) {

        FavoritesVideo favoritesVideo = new FavoritesVideo();
        favoritesVideo.setFavoritesId(favoritesId);
        favoritesVideo.setVideoId(videoId);
        favoritesVideo.setUserId(userId);
        try {
            favoritesVideoService.save(favoritesVideo);
        } catch (Exception e) {
            favoritesVideoService.remove(new LambdaQueryWrapper<FavoritesVideo>()
                    .eq(FavoritesVideo::getFavoritesId, favoritesId)
                    .eq(FavoritesVideo::getVideoId, videoId)
                    .eq(FavoritesVideo::getUserId, userId));
            return false;
        }
        return true;
    }

    @Override
    public Boolean favoritesState(Long videoId, Long userId) {
        if (userId == null) {
            return false;
        }
        return favoritesVideoService.count(new LambdaQueryWrapper<FavoritesVideo>()
                .eq(FavoritesVideo::getVideoId, videoId)
                .eq(FavoritesVideo::getUserId, userId)) == 1;
    }

    @Override
    public void exist(Long userId, Long fId) {
        int count = (int) count(new LambdaQueryWrapper<Favorites>().eq(Favorites::getUserId, userId).eq(Favorites::getId, fId));
        if (count == 0) {
            throw new BaseException("收藏夹选择错误");
        }
    }
}
