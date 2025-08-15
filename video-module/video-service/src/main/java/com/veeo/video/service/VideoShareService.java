package com.veeo.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.veeo.common.entity.video.VideoShare;

import java.util.List;


public interface VideoShareService extends IService<VideoShare> {

    /**
     * 添加分享记录
     */
    boolean share(VideoShare videoShare);


    /**
     * 获取分享用户id
     */
    List<Long> getShareUserId(Long videoId);
}
