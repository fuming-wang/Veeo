package com.veeo.video.client;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

@FeignClient(value = "video-service", contextId = "video-client", path = "/api/video")
public interface VideoClient {


    @RequestMapping("/listByUserIdVideo")
    Result<IPage<Video>> listByUserIdVideo(@RequestBody BasePage basePage, @RequestParam("userId") Long userId);

    @RequestMapping("/listVideoIdByUserId")
    Result<Collection<Long>> listVideoIdByUserId(@RequestParam Long userId);
}
