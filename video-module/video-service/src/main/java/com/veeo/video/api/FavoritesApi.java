package com.veeo.video.api;

import com.veeo.common.entity.user.Favorites;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.video.client.FavoritesClient;
import com.veeo.video.service.FavoritesService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/favorites")
public class FavoritesApi implements FavoritesClient {

    @Resource
    private FavoritesService favoritesService;

    @Override
    public Result<Favorites> save(Favorites favorites) {
        favoritesService.save(favorites);
        return ResultUtil.getSucRet(favorites);

    }

    @Override
    public void exist(Long userId, Long fId) {
        favoritesService.exist(userId, fId);
    }
}
