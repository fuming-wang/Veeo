package com.veeo.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.veeo.common.entity.video.VideoStar;

import java.util.List;


public interface VideoStarService extends IService<VideoStar> {

    /**
     * 视频点赞
     */
    boolean starVideo(VideoStar videoStar);

    /**
     * 视频点赞用户
     */
    List<Long> getStarUserIds(Long videoId);

    /**
     * 点赞状态
     */
    Boolean starState(Long videoId, Long userId);
}
