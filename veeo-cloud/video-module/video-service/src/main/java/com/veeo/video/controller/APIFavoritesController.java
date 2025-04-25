package com.veeo.video.controller;

import com.veeo.common.entity.user.Favorites;
import com.veeo.video.api.FavoritesClient;
import com.veeo.video.service.FavoritesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/favorites")
public class APIFavoritesController implements FavoritesClient {

    private final FavoritesService favoritesService;

    public APIFavoritesController(FavoritesService favoritesService) {
        this.favoritesService = favoritesService;
    }

    @Override
    public boolean save(Favorites favorites) {
        if (favorites == null) {
            log.error("save favorites is null");
        }
        return favoritesService.save(favorites);
    }

    @Override
    public void exist(Long userId, Long fId) {
        if (fId == null || userId == null) {
            log.error("userId or fId  is null");
        }
        favoritesService.exist(userId, fId);
    }
}
