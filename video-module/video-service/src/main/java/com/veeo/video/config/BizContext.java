package com.veeo.video.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.veeo.common.entity.Setting;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.video.VideoShare;
import com.veeo.common.entity.vo.BasePage;
import lombok.Data;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @ClassName BizContext
 * @Description
 * @Author wangfuming
 * @Date 2025/7/23 21:56
 * @Version 1.0.0
 */
@Data
public class BizContext {

    private Boolean result;
    private Long typeId;
    private Long userId;
    private Long videoId;
    private Video video;
    private Collection<Long> videoIds;
    private Collection<Video> videos;
    private LinkedHashMap<String, List<Video>> videoMap;
    private Long favoriteId;

    private String search;
    private VideoShare videoShare;
    private BasePage basePage;

    private IPage<Video> videoPage;

    private IPage<Type> typePage;

    private Long lastTime;

    private Setting setting;

    private BizContext() {}

    public static BizContext create() {
        return new BizContext();
    }

}