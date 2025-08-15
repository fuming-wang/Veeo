package com.veeo.video.handler.video;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.UserVO;
import com.veeo.common.exception.BaseException;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.common.util.Result;
import com.veeo.user.client.UserClient;
import com.veeo.video.config.BizContext;
import com.veeo.video.constant.RedisConstant;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.TypeService;
import com.veeo.video.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @ClassName historyVideoHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/26 11:22
 * @Version 1.0.0
 */
@Service
public class HistoryVideoHandler implements BizHandler {

    @Resource
    private VideoService videoService;

    @Resource
    private RedisCacheUtil redisCacheUtil;

    @Resource
    private UserClient userClient;

    @Resource
    private TypeService typeService;

    @Override
    public void handle(BizContext context) {
        Long userId = context.getUserId();
        Long videoId = context.getVideoId();
        Video video = context.getVideo(); // 前置需要video
        String key = RedisConstant.HISTORY_VIDEO + videoId + ":" + userId;
        Object o = redisCacheUtil.get(key);
        if (o != null) {
            return;
        }
        redisCacheUtil.set(key, videoId, RedisConstant.HISTORY_TIME);
        Result<UserVO> userResult = userClient.getInfo(video.getUserId());
        if (userResult.isFailed()) {
            throw new BaseException(userResult.getMessage());
        }
        video.setUser(userResult.getData());
        video.setTypeName(typeService.getById(video.getTypeId()).getName());
        redisCacheUtil.zadd(RedisConstant.USER_HISTORY_VIDEO + userId,
                new Date().getTime(), video, RedisConstant.HISTORY_TIME);
        updateHistory(video, 1L);
    }

    public void updateHistory(Video video, Long value) {
        UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("history_count = history_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getHistoryCount, video.getHistoryCount());

        videoService.update(video, updateWrapper);
    }
}
