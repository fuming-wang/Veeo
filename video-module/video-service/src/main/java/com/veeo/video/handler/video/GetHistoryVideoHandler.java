package com.veeo.video.handler.video;

import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.video.config.BizContext;
import com.veeo.video.constant.RedisConstant;
import com.veeo.video.handler.BizHandler;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName GetHistoryVideoHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/26 11:32
 * @Version 1.0.0
 */
@Service
public class GetHistoryVideoHandler implements BizHandler {


    @Resource
    private RedisCacheUtil redisCacheUtil;


    @Override
    public void handle(BizContext context) {
        Long userId = context.getUserId();
        BasePage basePage = context.getBasePage();
        String key = RedisConstant.USER_HISTORY_VIDEO + userId;

        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisCacheUtil.zSetGetByPage(key, basePage.getPage(), basePage.getLimit());
        if (ObjectUtils.isEmpty(typedTuples)) {
            context.setVideoMap(new LinkedHashMap<>());
            context.setVideoIds(Collections.emptyList());
            return;
        }
        List<Video> videoList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        LinkedHashMap<String, List<Video>> result = new LinkedHashMap<>();
        for (ZSetOperations.TypedTuple<Object> typedTuple : typedTuples) {
            Date date = new Date(typedTuple.getScore().longValue());
            String format = simpleDateFormat.format(date);
            if (!result.containsKey(format)) {
                result.put(format, new ArrayList<>());
            }
            Video video = (Video) typedTuple.getValue();
            result.get(format).add(video);
            videoList.add(video);
        }
        context.setVideos(videoList);
        context.setVideoMap(result);
    }
}
