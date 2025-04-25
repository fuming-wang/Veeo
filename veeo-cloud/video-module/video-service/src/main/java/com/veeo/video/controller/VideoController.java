package com.veeo.video.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.holder.UserHolder;
import com.veeo.common.limit.Limit;
import com.veeo.common.query.QueryCondition;
import com.veeo.common.query.QueryDTO;
import com.veeo.common.query.QueryWrapperUtil;
import com.veeo.common.util.R;
import com.veeo.file.api.QiNiuFileClient;
import com.veeo.user.api.UserClient;
import com.veeo.video.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.text.ParseException;
import java.util.Arrays;
import java.util.List;


@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/veeo/video")
public class VideoController {

    private final VideoService videoService;

    private final QiNiuFileClient qiNiuFileAPI;




    public VideoController(VideoService videoService, QiNiuFileClient qiNiuFileAPI) {
        this.videoService = videoService;
        this.qiNiuFileAPI = qiNiuFileAPI;
    }

    /**
     * 获取文件上传token
     */
    @GetMapping("/token")
    public R getToken(){
        return R.ok().data(qiNiuFileAPI.getToken());
    }

    /**
     * 发布视频/修改视频
     */
    @PostMapping
    @Limit(limit = 5, time = 3600L, msg = "发布视频一小时内不可超过5次")
    public R publishVideo(@RequestBody @Validated Video video){
        videoService.publishVideo(video);
        return R.ok().message("发布成功,请等待审核");
    }

    /**
     * 删除视频
     */
    @DeleteMapping("/{id}")
    public R deleteVideo(@PathVariable Long id){
        videoService.deleteVideo(id);
        return R.ok().message("删除成功");
    }

    /**
     * 查看用户所管理的视频 -稿件管理
     */
    @GetMapping
    public R listVideo(BasePage basePage){
        log.debug("basePage:{}", basePage);
        log.debug("UserHolder: {}", UserHolder.get());
        IPage<Video> videoIPage = videoService.listByUserIdVideo(basePage, UserHolder.get());
        log.debug("videoIPage:{}", videoIPage);
        log.debug("videoIPage.getTotal():{}", videoIPage.getTotal());
        return R.ok().data(videoIPage);
    }


    /**
     * 点赞视频
     */
    @PostMapping("/star/{id}")
    public R starVideo(@PathVariable Long id){
        String msg = "已点赞";
        if (!videoService.startVideo(id)) {
            msg = "取消点赞";
        }
        return R.ok().message(msg);
    }

    /**
     * 添加浏览记录
     */
    @PostMapping("/history/{id}")
    public R addHistory(@PathVariable Long id) throws Exception {
        videoService.historyVideo(id, UserHolder.get());
        return R.ok();
    }

    /**
     * 获取用户的浏览记录
     */
    @GetMapping("/history")
    public R getHistory(BasePage basePage){
        return R.ok().data(videoService.getHistory(basePage));
    }

    /**
     * 获取收藏夹下的视频
     */
    @GetMapping("/favorites/{favoritesId}")
    public R listVideoByFavorites(@PathVariable Long favoritesId){
        return R.ok().data(videoService.listVideoByFavorites(favoritesId));
    }

    /**
     * 收藏视频
     */
    @PostMapping("/favorites/{fId}/{vId}")
    public R favoritesVideo(@PathVariable Long fId,@PathVariable Long vId){
        String msg = videoService.favoritesVideo(fId,vId) ? "已收藏" : "取消收藏";
        return R.ok().message(msg);
    }

    /**
     * 返回当前审核队列状态
     */
    @GetMapping("/audit/queue/state")
    public R getAuditQueueState(){
        return R.ok().message(videoService.getAuditQueueState());
    }


    /**
     * 推送关注的人视频[拉模式]
     */
    @GetMapping("/follow/feed")
    public R followFeed(@RequestParam(required = false) Long lastTime) throws ParseException {
        Long userId = UserHolder.get();

        return R.ok().data(videoService.followFeed(userId, lastTime));
    }

    /**
     * 初始化收件箱
     */
    @PostMapping("/init/follow/feed")
    public R initFollowFeed(){
        Long userId = UserHolder.get();
        videoService.initFollowFeed(userId);
        return R.ok();
    }

}

