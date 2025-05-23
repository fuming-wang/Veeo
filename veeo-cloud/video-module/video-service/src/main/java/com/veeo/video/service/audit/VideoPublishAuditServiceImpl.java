package com.veeo.video.service.audit;

import com.veeo.common.config.QiNiuConfig;
import com.veeo.common.constant.AuditStatus;
import com.veeo.common.entity.response.AuditResponse;
import com.veeo.common.entity.task.VideoTask;
import com.veeo.common.entity.video.Video;
import com.veeo.file.api.FileClient;
import com.veeo.file.api.QiNiuFileClient;
import com.veeo.interest.api.FeedClient;
import com.veeo.interest.api.InterestClient;
import com.veeo.user.api.FollowClient;
import com.veeo.video.mapper.VideoMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;


import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @description: 视频发布审核
 */
@Service
public class VideoPublishAuditServiceImpl implements AuditService<VideoTask, VideoTask> , InitializingBean,BeanPostProcessor {

    @Autowired
    private FeedClient feedService;
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private InterestClient interestAPI;
    @Autowired
    private QiNiuFileClient qiNiuFileAPI;
    @Autowired
    private TextAuditService textAuditService;
    @Autowired
    private ImageAuditService imageAuditService;
    @Autowired
    private VideoAuditService videoAuditService;
    @Autowired
    private FollowClient followClient;
    @Autowired
    private FileClient fileAPI;

    private int maximumPoolSize = 8;

    protected ThreadPoolExecutor executor;



    /**
     *
     * @param videoTask
     * @param auditQueueState 申请快/慢审核
     * @return
     */
    public VideoTask audit(VideoTask videoTask,Boolean auditQueueState){

        if (auditQueueState){
            new Thread(()->{
                audit(videoTask);
            }).start();
        }else {
            audit(videoTask);
        }
        return null;
    }

    // 进行任务编排
    @Override
    public VideoTask audit(VideoTask videoTask) {
        executor.submit(()->{
            Video video = videoTask.getVideo();
            Video video1 = new Video();
            BeanUtils.copyProperties(video,video1);
            // 只有视频在新增或者公开时候才需要调用审核视频/封面
            // 新增 ： 必须审核
            // 修改: 新老状态不一致
            // 需要审核视频/封面
            boolean needAuditVideo = false;
            if (videoTask.getIsAdd()  && videoTask.getOldState() == videoTask.getNewState()){
                needAuditVideo = true;
            }else if (!videoTask.getIsAdd() && videoTask.getOldState() != videoTask.getNewState()){
                // 修改的情况下新老状态不一致,说明需要更新
                if (!videoTask.getNewState()){
                   needAuditVideo = true;
                }
            }
            AuditResponse videoAuditResponse = new AuditResponse(AuditStatus.SUCCESS,"正常");
            AuditResponse coverAuditResponse = new AuditResponse(AuditStatus.SUCCESS,"正常");
            AuditResponse titleAuditResponse = new AuditResponse(AuditStatus.SUCCESS,"正常");
            AuditResponse descAuditResponse = new AuditResponse(AuditStatus.SUCCESS,"正常");

            if (needAuditVideo){

                  videoAuditResponse = videoAuditService.audit(QiNiuConfig.CNAME+"/"+ fileAPI.getById(video.getUrl()).getFileKey());
                  coverAuditResponse = imageAuditService.audit(QiNiuConfig.CNAME+"/"+ fileAPI.getById(video.getCover()).getFileKey());
                interestAPI.pushSystemTypeStockIn(video1);
                interestAPI.pushSystemStockIn(video1);

                // 推入发件箱
                feedService.pushOutBoxFeed(video.getUserId(),video.getId(),video1.getGmtCreated().getTime());
            }else if (videoTask.getNewState()){
                interestAPI.deleteSystemStockIn(video1);
                interestAPI.deleteSystemTypeStockIn(video1);
                // 删除发件箱以及收件箱
                Collection<Long> fans = followClient.getFans(video.getUserId(), null);
                feedService.deleteOutBoxFeed(video.getUserId(),fans,video.getId());
            }

            // 新老视频标题简介一致
            final Video oldVideo = videoTask.getOldVideo();
            if (!video.getTitle().equals(oldVideo.getTitle())) {
                titleAuditResponse = textAuditService.audit(video.getTitle());
            }
            if (!video.getDescription().equals(oldVideo.getDescription()) && !ObjectUtils.isEmpty(video.getDescription())){
                descAuditResponse = textAuditService.audit(video.getDescription());
            }

            final Integer videoAuditStatus = videoAuditResponse.getAuditStatus();
            final Integer coverAuditStatus = coverAuditResponse.getAuditStatus();
            final Integer titleAuditStatus = titleAuditResponse.getAuditStatus();
            final Integer descAuditStatus = descAuditResponse.getAuditStatus();
            boolean f1 = videoAuditStatus == AuditStatus.SUCCESS;
            boolean f2 = coverAuditStatus == AuditStatus.SUCCESS;
            boolean f3 = titleAuditStatus == AuditStatus.SUCCESS;
            boolean f4 = descAuditStatus == AuditStatus.SUCCESS;

            if (f1 && f2 && f3 && f4) {
                video1.setMsg("通过");
                video1.setAuditStatus(AuditStatus.SUCCESS);
                // 填充视频时长
            }else {
                video1.setAuditStatus(AuditStatus.PASS);
                // 避免干扰
                video1.setMsg("");
                if (!f1){
                    video1.setMsg("视频有违规行为: "+videoAuditResponse.getMsg());
                }
                if (!f2){
                    video1.setMsg(video1.getMsg()+"\n封面有违规行为: " + coverAuditResponse.getMsg());
                }
                if (!f3){
                    video1.setMsg(video1.getMsg()+"\n标题有违规行为: " + titleAuditResponse.getMsg());
                }
                if (!f4){
                    video1.setMsg(video1.getMsg()+"\n简介有违规行为: " + descAuditResponse.getMsg());
                }
            }

            videoMapper.updateById(video1);
        });

        return null;
    }
    public boolean getAuditQueueState(){
        return executor.getTaskCount() < maximumPoolSize;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        executor  = new ThreadPoolExecutor(5, maximumPoolSize, 60, TimeUnit.SECONDS, new ArrayBlockingQueue(1000));
    }
}
