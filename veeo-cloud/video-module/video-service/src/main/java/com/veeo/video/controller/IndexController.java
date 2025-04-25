package com.veeo.video.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.video.VideoShare;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.util.JwtUtils;
import com.veeo.common.util.R;
import com.veeo.user.api.UserClient;
import com.veeo.video.service.TypeService;
import com.veeo.video.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/veeo/index")
public class IndexController {

    private final UserClient userClient;

    private final VideoService videoService;

    private final TypeService typeService;

    public IndexController(UserClient userClient, VideoService videoService, TypeService typeService) {
        this.userClient = userClient;
        this.videoService = videoService;
        this.typeService = typeService;
    }

    /**
     * 兴趣推送视频
     */
    @GetMapping("/pushVideos")
    public R pushVideos(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        return R.ok().data(videoService.pushVideos(userId));
    }

    /**
     * 搜索视频
     */
    @GetMapping("/search")
    public R searchVideo(@RequestParam(required = false) String searchName, BasePage basePage, HttpServletRequest request){
        String token = request.getHeader("token");
        return R.ok().data(videoService.searchVideo(searchName, basePage, JwtUtils.getUserId(token)));
    }

    /**
     * 根据视频分类获取
     */
    @GetMapping("/video/type/{typeId}")
    public R getVideoByTypeId(@PathVariable Long typeId){

        return R.ok().data(videoService.getVideoByTypeId(typeId));
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/types")
    public R getTypes(HttpServletRequest request){
        // FIXME 这里需要token, 如果没有token显然会报错
        String token = request.getHeader("token");
        List<Type> types = typeService.list(new LambdaQueryWrapper<Type>().select(Type::getIcon, Type::getId, Type::getName).orderByDesc(Type::getSort));

        Set<Long> set = userClient.listSubscribeType(JwtUtils.getUserId(token)).stream().map(Type::getId).collect(Collectors.toSet());

        for (Type type : types) {
            if (set.contains(type.getId())) {
                type.setUsed(true);
            }else {
                type.setUsed(false);
            }
        }
        return R.ok().data(types);
    }

    /**
     * 分享视频
     */
    @PostMapping("/share/{videoId}")
    public R share(@PathVariable Long videoId, HttpServletRequest request){

        String ip = null;
        if (request.getHeader("x-forwarded-for") == null)
            ip = request.getRemoteAddr();
        else
            ip = request.getHeader("x-forwarded-for");
        VideoShare videoShare = new VideoShare();

        videoShare.setVideoId(videoId);
        videoShare.setIp(ip);
        String token = request.getHeader("token");
        if (JwtUtils.checkToken(token)) {
            videoShare.setUserId(JwtUtils.getUserId(token));
        }
        videoService.shareVideo(videoShare);
        return R.ok();
    }

    /**
     * 根据id获取视频详情
     */
    @GetMapping("/video/{id}")
    public R getVideoById(@PathVariable Long id, HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        return R.ok().data(videoService.getVideoById(id, userId));
    }

    /**
     * 获取热度排行榜
     */
    @GetMapping("/video/hot/rank")
    public R listHotRank(){
        return R.ok().data(videoService.hotRank());
    }

    /**
     * 根据视频标签推送相似视频
     */
    @GetMapping("/video/similar")
    public R pushVideoSimilar(Video video){
        log.warn("video similar {}", video);
        return R.ok().data(videoService.listSimilarVideo(video));
    }

    /**
     * 推送热门视频
     */
    @GetMapping("/video/hot")
    public R listHotVideo(){
        return R.ok().data(videoService.listHotVideo());
    }

    /**
     * 根据用户id获取视频
     */
    @GetMapping("/video/user")
    public R listVideoByUserId(@RequestParam(required = false) Long userId,
                               BasePage basePage, HttpServletRequest request){

        String token = request.getHeader("token");
        userId = userId == null ? JwtUtils.getUserId(token) : userId;
        log.debug("userId: {}", userId);
        return R.ok().data(videoService.listByUserIdOpenVideo(userId, basePage));
    }

    /**
     * 获取用户搜索记录
     */
    @GetMapping("/search/history")
    public R searchHistory(HttpServletRequest request){
        // FIXME 这里也需要token, 如果没有token会报错
        String token = request.getHeader("token");
        return R.ok().data(userClient.searchHistory(JwtUtils.getUserId(token)));
    }

    /**
     * 删除搜索记录
     */
    @DeleteMapping("/search/history")
    public R deleteSearchHistory(HttpServletRequest request){
        String token = request.getHeader("token");
        userClient.deleteSearchHistory(JwtUtils.getUserId(token));
        return R.ok();
    }
}
