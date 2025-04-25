package com.veeo.video.schedul;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeo.common.constant.AuditStatus;
import com.veeo.common.constant.RedisConstant;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.HotVideo;
import com.veeo.common.query.QueryDTO;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.sys.api.SettingClient;
import com.veeo.video.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;


import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * description: 热度排行榜
 */
@Component
public class HotRank {


    @Autowired
    private VideoService videoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SettingClient settingClient;

    @Autowired
    private RedisCacheUtil redisCacheUtil;


    ObjectMapper objectMapper = new ObjectMapper();
    GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);


    /**
     * 热度排行榜
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void hotRank() {
        // 控制数量
        TopK topK = new TopK(10, new PriorityQueue<HotVideo>(10, Comparator.comparing(HotVideo::getHot)));
        long limit = 1000;
        // 每次拿1000个
        long id = 0;
        List<Video> videos = videoService.list(new LambdaQueryWrapper<Video>()
                .select(Video::getId, Video::getShareCount, Video::getHistoryCount, Video::getStartCount, Video::getFavoritesCount,
                        Video::getGmtCreated, Video::getTitle).gt(Video::getId, id)
                .eq(Video::getAuditStatus, AuditStatus.SUCCESS).eq(Video::getOpen, 0).last("limit " + limit));

        while (!ObjectUtils.isEmpty(videos)) {
            for (Video video : videos) {
                Long shareCount = video.getShareCount();
                Double historyCount = video.getHistoryCount() * 0.8;
                Long startCount = video.getStartCount();
                Double favoritesCount = video.getFavoritesCount() * 1.5;
                Date date = new Date();
                long t = date.getTime() - video.getGmtCreated().getTime();
                // 随机获取6位数,用于去重
                double v = weightRandom();
                double hot = hot(shareCount + historyCount + startCount + favoritesCount + v, TimeUnit.MILLISECONDS.toDays(t));
                HotVideo hotVideo = new HotVideo(hot, video.getId(), video.getTitle());
                topK.add(hotVideo);
            }
            id = videos.get(videos.size() - 1).getId();
            videos = videoService.list(new LambdaQueryWrapper<Video>().gt(Video::getId, id).last("limit " + limit));
        }
        byte[] key = RedisConstant.HOT_RANK.getBytes();
        List<HotVideo> hotVideos = topK.get();
        Double minHot = hotVideos.get(0).getHot();
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
        redisTemplate.opsForZSet().removeRangeByScore(RedisConstant.HOT_RANK, minHot,0);


    }

    // 热门视频,没有热度排行榜实时且重要
    @Scheduled(cron = "0 0 */3 * * ?")
    public void hotVideo() {
        // 分片查询3天内的视频
        int limit = 1000;
        long id = 1;
        List<Video> videos = videoService.selectNDaysAgeVideo(id, 3, limit);

        // 空条件查询
        QueryDTO queryDTO = new QueryDTO();
        Double hotLimit = settingClient.list(queryDTO).get(0).getHotLimit();
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DATE);

        while (!ObjectUtils.isEmpty(videos)) {
            List<Long> hotVideos = new ArrayList<>();
            for (Video video : videos) {
                Long shareCount = video.getShareCount();
                Double historyCount = video.getHistoryCount() * 0.8;
                Long startCount = video.getStartCount();
                Double favoritesCount = video.getFavoritesCount() * 1.5;
                Date date = new Date();
                long t = date.getTime() - video.getGmtCreated().getTime();
                double hot = hot(shareCount + historyCount + startCount + favoritesCount, TimeUnit.MILLISECONDS.toDays(t));
                // 大于X热度说明是热门视频
                if (hot > hotLimit) {
                    hotVideos.add(video.getId());
                }
            }
            id = videos.get(videos.size() - 1).getId();
            videos = videoService.selectNDaysAgeVideo(id, 3, limit);
            // RedisConstant.HOT_VIDEO + 今日日期 作为key  达到元素过期效果
            if (!ObjectUtils.isEmpty(hotVideos)){
                String key = RedisConstant.HOT_VIDEO + today;
                redisTemplate.opsForSet().add(key, hotVideos.toArray(new Object[hotVideos.size()]));
                redisTemplate.expire(key, 3, TimeUnit.DAYS);
            }

        }


    }

    static double a = 0.011;

    public static double hot(double weight, double t) {
        return weight * Math.exp(-a * t);
    }


    public double weightRandom() {
        int i = (int) ((Math.random() * 9 + 1) * 100000);
        return i / 1000000.0;
    }

}
