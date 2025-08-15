package com.veeo.video.client;


import com.veeo.common.entity.user.Favorites;
import com.veeo.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(value = "video-service", contextId = "favorites-client", path = "/api/favorites")
public interface FavoritesClient {

    @RequestMapping("/save")
    Result<Favorites> save(@RequestBody Favorites favorites);

    @RequestMapping("/exist")
    void exist(@RequestParam Long userId, @RequestParam Long fId);
}
