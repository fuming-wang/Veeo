package com.veeo.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.entity.video.VideoStar;
import com.veeo.video.mapper.VideoStarMapper;
import com.veeo.video.service.VideoStarService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class VideoStarServiceImpl extends ServiceImpl<VideoStarMapper, VideoStar> implements VideoStarService {


    private final VideoStarMapper videoStarMapper;

    public VideoStarServiceImpl(VideoStarMapper videoStarMapper) {
        this.videoStarMapper = videoStarMapper;
    }

    @Override
    public boolean starVideo(VideoStar videoStar) {

        VideoStar existVideoStar = videoStarMapper.selectAllStatesByUserAndVideo(videoStar.getVideoId(), videoStar.getUserId());
        // 1. 没有点过赞, 点赞就行
        if (existVideoStar == null) {
            this.save(videoStar);
            return true;
        }

        Boolean isDeleted = existVideoStar.getIsDeleted();
        // 2. 点过赞但是被取消了, 更新就行
        if (isDeleted) {
            videoStarMapper.updateExistStarStatusById(existVideoStar.getId(), 0);
            return true;
        }
        // 3. 点过赞现在要取消点赞
        videoStarMapper.updateExistStarStatusById(existVideoStar.getId(), 1);
        return false;
    }


    @Override
    public List<Long> getStarUserIds(Long videoId) {
        return this.list(new LambdaQueryWrapper<VideoStar>()
                        .eq(VideoStar::getVideoId, videoId))
                .stream().map(VideoStar::getUserId).collect(Collectors.toList());
    }

    @Override
    public Boolean starState(Long videoId, Long userId) {

        if (userId == null) {
            return false;
        }
        return this.count(new LambdaQueryWrapper<VideoStar>().eq(VideoStar::getVideoId, videoId).eq(VideoStar::getUserId, userId)) == 1;
    }
}
