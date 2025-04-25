package com.veeo.video.api;


import com.veeo.common.entity.user.Favorites;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(value = "video-service", contextId = "favorites-client", path = "/api/favorites")
public interface FavoritesClient {


    @RequestMapping("/save")
    boolean save(@RequestBody Favorites favorites);

    @RequestMapping("/exist")
    void exist(@RequestParam Long userId, @RequestParam Long fId);
}
