package com.veeo.video.handler.video;

import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.video.Video;
import com.veeo.common.util.Result;
import com.veeo.interest.client.FeedClient;
import com.veeo.interest.client.InterestClient;
import com.veeo.user.client.FollowClient;
import com.veeo.video.config.BizContext;
import com.veeo.video.constant.AuditStatus;
import com.veeo.video.constant.RedisConstant;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.TypeService;
import com.veeo.video.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

/**
 * @ClassName BanVideoHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/25 22:04
 * @Version 1.0.0
 */
@Service
public class BanVideoHandler implements BizHandler {

    @Resource
    private VideoService videoService;

    @Resource
    private TypeService typeService;

    @Resource
    private InterestClient interestClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private FeedClient feedClient;

    @Resource
    private FollowClient followClient;

    @Override
    public void handle(BizContext context) {
        Video video = context.getVideo();
        Long id = video.getId();

        Type type = typeService.getById(video.getTypeId());

        video.setLabelNames(type.getLabelNames());
        // 修改视频信息
        video.setOpen(true);
        video.setMsg("该视频违反了Veeo平台的规则, 已被下架私密");
        video.setAuditStatus(AuditStatus.PASS);
        // 删除分类中的视频
        interestClient.deleteSystemTypeStockIn(video);
        // 删除标签中的视频
        interestClient.deleteSystemStockIn(video);
        // 获取视频发布者id,删除对应的发件箱
        Long userId = video.getUserId();

        redisTemplate.opsForZSet().remove(RedisConstant.OUT_FOLLOW + userId, id);

        // 获取视频发布者粉丝，删除对应的收件箱
        Result<Collection<Long>> fansResult = followClient.getFollow(userId);
        if (fansResult.isFailed()) {
            throw new RuntimeException(fansResult.getMessage());
        }
        Collection<Long> fansIds = fansResult.getData();
        feedClient.deleteInBoxFeed(userId, Collections.singletonList(id));
        feedClient.deleteOutBoxFeed(userId, fansIds, id);

        // 热门视频以及热度排行榜视频
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DATE);
        Long videoId = video.getId();
        // 尝试去找到删除
        redisTemplate.opsForSet().remove(RedisConstant.HOT_VIDEO + today, videoId);
        redisTemplate.opsForSet().remove(RedisConstant.HOT_VIDEO + (today - 1), videoId);
        redisTemplate.opsForSet().remove(RedisConstant.HOT_VIDEO + (today - 2), videoId);
        redisTemplate.opsForZSet().remove(RedisConstant.HOT_RANK, videoId);
        videoService.updateById(video);
    }
}
