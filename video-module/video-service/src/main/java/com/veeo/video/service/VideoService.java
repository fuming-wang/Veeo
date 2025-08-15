package com.veeo.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.veeo.common.entity.video.Video;
import com.veeo.common.util.Result;

import java.util.Collection;
import java.util.List;


public interface VideoService extends IService<Video> {


    Result<Collection<Video>> scanVideo(Long startId, Long batchSize);

    /**
     * 获取N天前的视频
     */
    List<Video> selectNDaysAgeVideo(long id, int days, int limit);

    /**
     * 获取该用户所发布的视频
     */
    Collection<Long> listVideoIdByUserId(Long userId);

}
