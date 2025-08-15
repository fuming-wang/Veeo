package com.veeo.video.service.audit;

import com.veeo.common.entity.File;
import com.veeo.common.entity.response.AuditResponse;
import com.veeo.common.entity.task.VideoTask;
import com.veeo.common.entity.video.Video;
import com.veeo.common.exception.BaseException;
import com.veeo.common.util.Result;
import com.veeo.file.client.AuditClient;
import com.veeo.file.client.FileClient;
import com.veeo.interest.client.FeedClient;
import com.veeo.interest.client.InterestClient;
import com.veeo.user.client.FollowClient;
import com.veeo.video.constant.AuditStatus;
import com.veeo.video.mapper.VideoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;


/**
 * @description: 视频发布审核
 */
@Slf4j
@Service
public class VideoPublishAuditService {

    public static final String CNAME = "http://qiniu.fmwang.asia";

    @Resource
    private FeedClient feedClient;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private InterestClient interestClient;

    @Resource
    private AuditClient auditClient;

    @Resource
    private FollowClient followClient;

    @Resource
    private FileClient fileClient;


    private boolean isNeedAudit(VideoTask videoTask) {
        /*
         * 只有视频在新增或者公开时候才需要调用审核视频/封面
         * 新增 ： 必须审核
         * 修改: 新老状态不一致
         * 需要审核视频/封面
         */
        if (videoTask.getIsAdd()) {
            // 新增视频必须审核
            return true;
        }
        return !Objects.equals(videoTask.getOldState(), videoTask.getNewState());
    }

    // 进行任务编排
    public void audit(VideoTask videoTask) {

        log.info("视频审核启动 id: {} url:{}", videoTask.getVideo().getId(), videoTask.getVideo().getUrl());
        Video video = videoTask.getVideo();
        Video video1 = new Video();
        BeanUtils.copyProperties(video, video1);

        boolean needAuditVideo = isNeedAudit(videoTask);

        AuditResponse videoAuditResponse = new AuditResponse(AuditStatus.SUCCESS, "正常");
        AuditResponse coverAuditResponse = new AuditResponse(AuditStatus.SUCCESS, "正常");
        AuditResponse titleAuditResponse = new AuditResponse(AuditStatus.SUCCESS, "正常");
        AuditResponse descAuditResponse  = new AuditResponse(AuditStatus.SUCCESS, "正常");

        if (needAuditVideo) {
            log.info("needAuditVideo {} {} {}", videoTask.getVideo().getUrl(), videoAuditResponse.getAuditStatus(),
                    coverAuditResponse.getAuditStatus());
            Collection<Long> fileIds = List.of(video.getUrl(), video1.getUrl());
            Result<Collection<File>> findFileResult = fileClient.listByIds(fileIds);
            if (findFileResult.isFailed()) {
                throw new BaseException(findFileResult.getMessage());
            }
            Map<Long, File> fileMap = new HashMap<>(4);
            findFileResult.getData().forEach(file -> fileMap.put(file.getId(), file));
            String url = CNAME + "/" + fileMap.get(video.getUrl()).getFileKey();
            Result<AuditResponse> videoResult = auditClient.videoAudit(url);

            if (videoResult.isFailed()) {
                throw new BaseException(videoResult.getMessage());
            }
            videoAuditResponse = videoResult.getData();

            url = CNAME + "/" + fileMap.get(video.getCover()).getFileKey();
            Result<AuditResponse> imageResult = auditClient.imageAudit(url);
            if (imageResult.isFailed()) {
                throw new BaseException(imageResult.getMessage());
            }
            coverAuditResponse = imageResult.getData();
            interestClient.pushSystemTypeStockIn(video1);
            interestClient.pushSystemStockIn(video1);
            // 推入发件箱
            feedClient.pushOutBoxFeed(video.getUserId(), video.getId(), video1.getGmtCreated().getTime());

        } else if (videoTask.getNewState()) {
            interestClient.deleteSystemStockIn(video1);
            interestClient.deleteSystemTypeStockIn(video1);
            // 删除发件箱以及收件箱
            Result<Collection<Long>> fansResult = followClient.getFans(video.getUserId());
            if (!fansResult.getState()) {
                throw new BaseException(fansResult.getMessage());
            }
            Collection<Long> fans = fansResult.getData();
            feedClient.deleteOutBoxFeed(video.getUserId(), fans, video.getId());
        }

        // 新老视频标题简介一致
        Video oldVideo = videoTask.getOldVideo();
        if (oldVideo == null || !video.getTitle().equals(oldVideo.getTitle())) {
            Result<AuditResponse> result = auditClient.textAudit(CNAME + "/" + video.getTitle());
            if (result.isFailed()) {
                throw new BaseException(result.getMessage());
            }
            titleAuditResponse = result.getData();
        }

        if (oldVideo == null ||
                (!ObjectUtils.isEmpty(video.getDescription()) &&
                        !video.getDescription().equals(oldVideo.getDescription()))) {
            Result<AuditResponse> result = auditClient.textAudit(CNAME + "/" + video.getDescription());
            if (result.isFailed()) {
                throw new BaseException(result.getMessage());
            }
            descAuditResponse = result.getData();
        }

        Integer videoAuditStatus = videoAuditResponse.getAuditStatus();
        Integer coverAuditStatus = coverAuditResponse.getAuditStatus();
        Integer titleAuditStatus = titleAuditResponse.getAuditStatus();
        Integer descAuditStatus = descAuditResponse.getAuditStatus();

        boolean f1 = videoAuditStatus.equals(AuditStatus.SUCCESS);
        boolean f2 = coverAuditStatus.equals(AuditStatus.SUCCESS);
        boolean f3 = titleAuditStatus.equals(AuditStatus.SUCCESS);
        boolean f4 = descAuditStatus.equals(AuditStatus.SUCCESS);
        if (f1 && f2 && f3 && f4) {
            video1.setMsg("通过");
            video1.setAuditStatus(AuditStatus.SUCCESS);
            // 填充视频时长
        } else {
            video1.setAuditStatus(AuditStatus.PASS);
            // 避免干扰
            StringBuilder errorMsg = new StringBuilder();
            if (!f1) {
                errorMsg.append("视频有违规行为: ").append(videoAuditResponse.getMsg());
            }
            if (!f2) {
                errorMsg.append("\n封面有违规行为: ").append(coverAuditResponse.getMsg());
            }
            if (!f3) {
                errorMsg.append("\n标题有违规行为: ").append(titleAuditResponse.getMsg());
            }
            if (!f4) {
                errorMsg.append("\n简介有违规行为: ").append(descAuditResponse.getMsg());
            }
            video1.setMsg(errorMsg.toString());
        }
        // 更新
        videoMapper.updateById(video1);
        log.info("视频审核完成 id: {} url:{}", videoTask.getVideo().getId(), videoTask.getVideo().getUrl());
    }

}
