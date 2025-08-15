package com.veeo.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.entity.user.FavoritesVideo;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.video.mapper.FavoritesVideoMapper;
import com.veeo.video.service.FavoritesVideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class FavoritesVideoServiceImpl extends ServiceImpl<FavoritesVideoMapper, FavoritesVideo> implements FavoritesVideoService {

    @Override
    public Result<Boolean> removeFavoritesVideo(Long favoritesId) {
        try {
            remove(new LambdaQueryWrapper<FavoritesVideo>().eq(FavoritesVideo::getFavoritesId, favoritesId));
        } catch (Exception e) {
            log.error("[FavoritesVideoServiceImpl] remove favoritesVideo error: {}", e.getMessage());
        }
        return ResultUtil.getSucRet(true);
    }
}
