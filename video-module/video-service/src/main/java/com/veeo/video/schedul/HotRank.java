package com.veeo.video.schedul;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.HotVideo;
import com.veeo.common.util.Result;
import com.veeo.video.constant.AuditStatus;
import com.veeo.video.constant.RedisConstant;
import com.veeo.video.service.SettingService;
import com.veeo.video.service.VideoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * description: 热度排行榜。如果是分布式部署呢? 获取锁的线程才可以更新
 */
@Slf4j
@Component
public class HotRank {

    static double a = 0.011;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

    @Resource
    private VideoService videoService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private SettingService settingService;

    public static double hot(double weight, double t) {
        return weight * Math.exp(-a * t);
    }

    private static final long BATCH_SIZE = 1000;

    /**
     * 热度排行榜
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void hotRank() {
        // 控制数量
        TopK topK = new TopK(10, new PriorityQueue<>(10, Comparator.comparing(HotVideo::getHot)));
        // 每次拿1000个
        long id = 0;

        Result<Collection<Video>> result = videoService.scanVideo(id, BATCH_SIZE);
        if (result.isFailed()) {
            result = videoService.scanVideo(id, BATCH_SIZE);
            if (result.isFailed()) {
                log.error("[HotRank] hotRank error {}", result.getMessage());
            }
        }
        List<Video> videos = (List<Video>) result.getData();

        while (!ObjectUtils.isEmpty(videos)) {
            videos.forEach(video -> {
                Double baseCount = getBaseDataCount(video);
                Date date = new Date();
                long t = date.getTime() - video.getGmtCreated().getTime();
                // 随机获取6位数,用于去重
                double v = weightRandom();
                double hot = hot(baseCount + v, TimeUnit.MILLISECONDS.toDays(t));
                HotVideo hotVideo = new HotVideo(hot, video.getId(), video.getTitle());
                topK.add(hotVideo);
            });
            id = videos.getLast().getId();
            result = videoService.scanVideo(id, BATCH_SIZE);
            videos = (List<Video>) result.getData();
        }
        byte[] key = RedisConstant.HOT_RANK.getBytes();
        List<HotVideo> hotVideos = topK.get();
        // 直接删除
        redisTemplate.delete(RedisConstant.HOT_RANK);
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (HotVideo hotVideo : hotVideos) {
                try {
                    Double hot = hotVideo.getHot();
                    hotVideo.setHot(null);
                    // 不这样写铁报错！序列化问题
                    connection.zAdd(key, hot, genericJackson2JsonRedisSerializer.serialize(objectMapper.writeValueAsString(hotVideo)));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        });

    }

    // 热门视频,没有热度排行榜实时且重要
    @Scheduled(cron = "0 */30 * * * ?")
    public void hotVideo() {
        // 分片查询3天内的视频
        int limit = 1000;
        long id = 1;
        List<Video> videos = videoService.selectNDaysAgeVideo(id, 3, limit);
        // 空条件查询
        Double hotLimit = settingService.list().getFirst().getHotLimit();
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DATE);

        while (!ObjectUtils.isEmpty(videos)) {
            List<Long> hotVideos = new ArrayList<>();
            videos.forEach(video -> {
                Double baseCount = getBaseDataCount(video);
                Date date = new Date();
                long t = date.getTime() - video.getGmtCreated().getTime();
                double hot = hot(baseCount, TimeUnit.MILLISECONDS.toDays(t));
                // 大于X热度说明是热门视频
                if (hot > hotLimit) {
                    hotVideos.add(video.getId());
                }
            });
            id = videos.getLast().getId();
            videos = videoService.selectNDaysAgeVideo(id, 3, limit);
            // RedisConstant.HOT_VIDEO + 今日日期 作为key 达到元素过期效果
            if (!ObjectUtils.isEmpty(hotVideos)) {
                String key = RedisConstant.HOT_VIDEO + today;
                redisTemplate.opsForSet().add(key, hotVideos.toArray());
                redisTemplate.expire(key, 3, TimeUnit.DAYS);
            }
        }


    }

    private Double getBaseDataCount(Video video) {
        Long shareCount = video.getShareCount();
        Double historyCount = video.getHistoryCount() * 0.8;
        Long startCount = video.getStartCount();
        Double favoritesCount = video.getFavoritesCount() * 1.5;
        return shareCount + historyCount + startCount + favoritesCount;
    }

    public double weightRandom() {
        int i = (int) ((Math.random() * 9 + 1) * 100000);
        return i / 1000000.0;
    }

}
