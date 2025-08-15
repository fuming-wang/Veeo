package com.veeo.video.handler.video;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veeo.common.config.LocalCache;
import com.veeo.common.constant.QiNiuConstant;
import com.veeo.common.entity.File;
import com.veeo.common.entity.task.VideoTask;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.video.Video;
import com.veeo.common.exception.BaseException;
import com.veeo.common.util.FileUtil;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.common.util.Result;
import com.veeo.file.client.FileClient;
import com.veeo.video.config.BizContext;
import com.veeo.video.constant.AuditStatus;
import com.veeo.video.constant.RedisConstant;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.TypeService;
import com.veeo.video.service.VideoService;
import com.veeo.video.service.audit.VideoPublishAuditService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * @ClassName publishVideoHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/26 14:11
 * @Version 1.0.0
 */
@Service
public class PublishVideoHandler implements BizHandler {

    @Resource
    private RedisCacheUtil redisCacheUtil;

    @Resource
    private VideoService videoService;

    @Resource
    private LocalCache localCache;

    @Resource
    private TypeService typeService;

    @Resource
    private FileClient fileClient;

    @Resource
    private VideoPublishAuditService videoPublishAuditService;

    @Override
    public void handle(BizContext context) {

        Long userId = context.getUserId();
        Video video = context.getVideo();
        Video oldVideo = null;
        // 不允许修改视频
        Long videoId = video.getId();
        if (videoId != null) {
            // 缓存中可能存在
            if ((oldVideo = (Video) redisCacheUtil.get(RedisConstant.VIDEO_CACHE + videoId)) == null) {
                oldVideo = videoService.getOne(new LambdaQueryWrapper<Video>()
                        .eq(Video::getId, videoId)
                        .eq(Video::getUserId, userId));
            }
            // url必须一致
            if (!(video.buildVideoUrl()).equals(oldVideo.buildVideoUrl()) ||
                    !(video.buildCoverUrl().equals(oldVideo.buildCoverUrl()))) {
                throw new BaseException("不能更换视频源, 只能修改视频信息");
            }
        }
        // 判断对应分类是否存在
        Type type = typeService.getById(video.getTypeId());
        if (type == null) {
            throw new BaseException("分类不存在");
        }
        // 校验标签最多不能超过5个
        if (video.buildLabel().size() > 5) {
            throw new BaseException("标签最多只能选择5个");
        }

        // 修改状态
        video.setAuditStatus(AuditStatus.PROCESS);
        video.setMsg("审核中");
        video.setUserId(userId);
        // 判断是不是新增视频
        boolean isAdd = videoId == null;

        // 校验
        video.setYv(null);

        if (!isAdd) {
            video.setVideoType(null);
            video.setLabelNames(null);
            video.setUrl(null);
            video.setCover(null);
        } else {
            // 如果没设置封面,我们帮他设置一个封面
            if (ObjectUtils.isEmpty(video.getCover())) {
                Result<Long> result = fileClient.generatePhoto(video.getUrl(), userId);
                // todo 应该是一个弱依赖？
                if (result.isFailed()) {
                    throw new BaseException(result.getMessage());
                }
                video.setCover(result.getData());
            }

            video.setYv("YV" + UUID.randomUUID().toString().replace("-", "").substring(8));
        }

        // 填充视频时长 (若上次发布视频不存在Duration则会尝试获取)
        if (isAdd || !StringUtils.hasLength(oldVideo.getDuration())) {
            String uuid = UUID.randomUUID().toString();
            localCache.put(uuid, true);
            try {
                Long url = video.getUrl();
                if (url == null || url == 0) {
                    url = oldVideo.getUrl();
                }
                Result<File> result = fileClient.getById(url);
                if (result.isFailed()) {
                    throw new BaseException(result.getMessage());
                }
                String fileKey = result.getData().getFileKey();
                String duration = FileUtil.getVideoDuration(QiNiuConstant.CNAME + "/" + fileKey + "?uuid=" + uuid);
                video.setDuration(duration);
            } finally {
                localCache.rem(uuid);
            }
        }
        videoService.saveOrUpdate(video);

        VideoTask videoTask = new VideoTask();
        videoTask.setOldVideo(oldVideo);
        videoTask.setVideo(video);
        videoTask.setIsAdd(isAdd);
        videoTask.setOldState(isAdd || video.getOpen());
        videoTask.setNewState(true);
        Thread.startVirtualThread(() -> videoPublishAuditService.audit(videoTask));
    }
}
