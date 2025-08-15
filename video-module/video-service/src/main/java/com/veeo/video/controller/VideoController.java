package com.veeo.video.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.exception.BaseException;
import com.veeo.common.holder.UserHolder;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.file.client.QiNiuFileClient;
import com.veeo.interest.client.FeedClient;
import com.veeo.user.client.FollowClient;
import com.veeo.video.biz.VideoBiz;
import com.veeo.video.config.BizContext;
import com.veeo.video.limit.Limit;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;


@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/veeo/video")
public class VideoController {


    @Resource
    private QiNiuFileClient qiNiuFileClient;

    @Resource
    private FollowClient followClient;

    @Resource
    private FeedClient feedClient;

    @Resource
    private VideoBiz videoBiz;


    /**
     * 获取文件上传token
     */
    @GetMapping("/token")
    public Result<String> getToken() {
        Result<String> token = qiNiuFileClient.getToken();
        if (!token.getState()) {
            throw new BaseException(token.getMessage());
        }
        return ResultUtil.getSucRet(token.getData());
    }

    /**
     * 发布视频/修改视频
     */
    @PostMapping
    @Limit(limit = 5, time = 3600L, msg = "发布视频一小时内不可超过5次")
    public Result<String> publishVideo(@RequestBody @Validated Video video) {
        BizContext context = BizContext.create();
        context.setVideo(video);
        videoBiz.publishVideo(context);
        return ResultUtil.getSucRet("发布成功,请等待审核");
    }

    /**
     * 删除视频
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteVideo(@PathVariable Long id) {
        BizContext context = BizContext.create();
        context.setUserId(UserHolder.get());
        context.setVideoId(id);
        videoBiz.deleteVideoById(context);
        return ResultUtil.getSucRet("删除成功");
    }

    /**
     * 查看用户所管理的视频 -稿件管理
     */
    @GetMapping
    public Result<IPage<Video>> listVideo(BasePage basePage) {
        BizContext context = BizContext.create();
        context.setUserId(UserHolder.get());
        context.setBasePage(basePage);
        videoBiz.listByUserIdVideo(context);
        return ResultUtil.getSucRet(context.getVideoPage());
    }

    /**
     * 点赞视频
     */
    @PostMapping("/star/{id}")
    public Result<Void> starVideo(@PathVariable Long id) {

        BizContext context = BizContext.create();
        context.setUserId(UserHolder.get());
        context.setVideoId(id);
        String msg = "已点赞";
        videoBiz.startVideo(context);
        if (context.getResult()) {
            msg = "取消点赞";
        }
        return ResultUtil.getSucRet(msg);
    }

    /**
     * 添加浏览记录
     */
    @PostMapping("/history/{id}")
    public Result<Void> addHistory(@PathVariable Long id) {
        BizContext context = BizContext.create();
        context.setVideoId(id);
        context.setUserId(UserHolder.get());
        videoBiz.addHistoryVideo(context);
        return ResultUtil.getSucRet();
    }

    /**
     * 获取用户的浏览记录
     */
    @GetMapping("/history")
    public Result<LinkedHashMap<String, List<Video>>> getHistory(BasePage basePage) {
        BizContext context = BizContext.create();
        context.setUserId(UserHolder.get());
        context.setBasePage(basePage);
        videoBiz.getHistoryVideo(context);
        return ResultUtil.getSucRet(context.getVideoMap());
    }

    /**
     * 获取收藏夹下的视频
     */
    @GetMapping("/favorites/{favoritesId}")
    public Result<Collection<Video>> listVideoByFavorites(@PathVariable Long favoritesId) {
        BizContext context = BizContext.create();
        context.setUserId(UserHolder.get());
        context.setFavoriteId(favoritesId);

        videoBiz.listVideoByFavorites(context);
        return ResultUtil.getSucRet(context.getVideos());
    }

    /**
     * 收藏视频
     */
    @PostMapping("/favorites/{fId}/{vId}")
    public Result<Void> favoritesVideo(@PathVariable Long fId, @PathVariable Long vId) {
        BizContext context = BizContext.create();
        context.setVideoId(vId);
        context.setFavoriteId(fId);
        context.setUserId(UserHolder.get());
        videoBiz.favoriteVideo(context);
        String msg = context.getResult() ? "已收藏" : "取消收藏";
        return ResultUtil.getSucRet(msg);
    }

    /**
     * 返回当前审核队列状态
     */
    @GetMapping("/audit/queue/state")
    public Result<String> getAuditQueueState() {
        return ResultUtil.getSucRet("快速");
    }


    /**
     * 推送关注的人视频[拉模式]
     */
    @GetMapping("/follow/feed")
    public Result<Collection<Video>> followFeed(@RequestParam(required = false) Long lastTime) {
        Long userId = UserHolder.get();
        BizContext context = BizContext.create();
        context.setUserId(userId);
        context.setLastTime(lastTime);
        videoBiz.followFeed(context);
        return ResultUtil.getSucRet(context.getVideos());
    }

    /**
     * 初始化收件箱
     */
    @PostMapping("/init/follow/feed")
    public Result<Void> initFollowFeed() {
        Long userId = UserHolder.get();
        // 获取所有关注的人
        Result<Collection<Long>> followResult = followClient.getFollow(userId);
        if (followResult.isFailed()) {
            throw new RuntimeException(followResult.getMessage());
        }
        feedClient.initFollowFeed(userId, followResult.getData());
        return ResultUtil.getSucRet();
    }

}

