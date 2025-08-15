package com.veeo.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.entity.video.Video;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.video.constant.AuditStatus;
import com.veeo.video.mapper.VideoMapper;
import com.veeo.video.service.VideoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {


    @Resource
    private VideoMapper videoMapper;


    @Override
    public Result<Collection<Video>> scanVideo(Long startId, Long batchSize) {
        Collection<Video> videoList;
        try {
            videoList = list(new LambdaQueryWrapper<Video>()
                    .select(Video::getId, Video::getShareCount, Video::getHistoryCount, Video::getStartCount,
                            Video::getFavoritesCount, Video::getGmtCreated, Video::getTitle).gt(Video::getId, startId)
                    .eq(Video::getAuditStatus, AuditStatus.SUCCESS)
                    .eq(Video::getOpen, 0)
                    .last("limit " + batchSize));
        } catch (Exception e) {
            log.error("[VideoService] scan video error", e);
            return ResultUtil.getFailRet(e.getMessage());
        }
        return ResultUtil.getSucRet(videoList);
    }


    // 需要保留
    @Override
    public List<Video> selectNDaysAgeVideo(long id, int days, int limit) {
        return videoMapper.selectNDaysAgeVideo(id, days, limit);
    }


    @Override
    public Collection<Long> listVideoIdByUserId(Long userId) {
        // todo
        return list(new LambdaQueryWrapper<Video>().eq(Video::getUserId, userId).eq(Video::getOpen, 0).select(Video::getId))
                .stream().map(Video::getId).collect(Collectors.toList());
    }

}
