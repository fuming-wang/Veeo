package com.veeo.video.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veeo.common.entity.video.Video;
import com.veeo.interest.client.InterestClient;
import com.veeo.video.constant.AuditStatus;
import com.veeo.video.constant.RedisConstant;
import com.veeo.video.service.VideoService;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Set;

/**
 * @ClassName LoadDataToRedis
 * @Description 将部分由Redis管理的数据在程序启动时加载到Redis中
 * @Author wangfuming
 * @Date 2025/6/2 14:07
 * @Version 1.0.0
 */
//@Component
public class LoadDataToRedis implements ApplicationRunner {


    @Resource
    private VideoService videoService;

    @Resource
    private InterestClient interestClient;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Set<String> set = redisTemplate.keys(RedisConstant.SYSTEM_STOCK + "*");
        redisTemplate.delete(set);
        set = redisTemplate.keys(RedisConstant.SYSTEM_TYPE_STOCK + "*");
        redisTemplate.delete(set);
        set = null; // 便与GC
        long limit = 1000;
        // 每次拿1000个
        long id = 0;
        List<Video> videos = videoService.list(new LambdaQueryWrapper<Video>()
                .select(Video::getId, Video::getTypeId, Video::getLabelNames).gt(Video::getId, id)
                .eq(Video::getAuditStatus, AuditStatus.SUCCESS).eq(Video::getOpen, 0).last("limit " + limit));
        while (!ObjectUtils.isEmpty(videos)) {
            for (Video video : videos) {
                System.out.println(video);
                interestClient.pushSystemTypeStockIn(video);
                interestClient.pushSystemStockIn(video);
            }
            id = videos.get(videos.size() - 1).getId();
            videos = videoService.list(new LambdaQueryWrapper<Video>()
                    .select(Video::getId, Video::getTypeId, Video::getLabelNames).gt(Video::getId, id)
                    .eq(Video::getAuditStatus, AuditStatus.SUCCESS).eq(Video::getOpen, 0).last("limit " + limit));
        }
    }
}
