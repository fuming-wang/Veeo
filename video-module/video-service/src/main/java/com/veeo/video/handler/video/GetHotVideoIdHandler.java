package com.veeo.video.handler.video;

import com.veeo.common.util.RedisCacheUtil;
import com.veeo.video.config.BizContext;
import com.veeo.video.constant.RedisConstant;
import com.veeo.video.handler.BizHandler;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * @ClassName GetHotVideoIdHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/26 13:27
 * @Version 1.0.0
 */

@Service
public class GetHotVideoIdHandler implements BizHandler {

    @Resource
    private RedisCacheUtil redisCacheUtil;


    @Override
    public void handle(BizContext context) {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DATE);

        HashMap<String, Integer> map = new HashMap<>();
        // 优先推送今日的
        map.put(RedisConstant.HOT_VIDEO + today, 10);
        map.put(RedisConstant.HOT_VIDEO + (today - 1), 3);
        map.put(RedisConstant.HOT_VIDEO + (today - 2), 2);

        // 游客不用记录
        // 获取今天日期
        List<Long> hotVideoIds = redisCacheUtil.pipeline(connection -> {
            map.forEach((k, v) -> {
                connection.setCommands().sRandMember(k.getBytes(), v);
            });
            return null;
        });

        if (ObjectUtils.isEmpty(hotVideoIds)) {
            context.setVideoIds(Collections.emptyList());
            return;
        }

        List<Long> videoIds = new ArrayList<>();
        // 会返回结果有null, 做下校验
        for (Object videoId : hotVideoIds) {
            if (!ObjectUtils.isEmpty(videoId)) {
                videoIds.addAll((List) videoId);
            }
        }
        if (ObjectUtils.isEmpty(videoIds)) {
            context.setVideoIds(Collections.emptyList());
            return;
        }
    }
}
