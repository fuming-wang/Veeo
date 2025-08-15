package com.veeo.video.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeo.common.constant.VeeoHttpConstant;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.video.VideoShare;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.entity.vo.HotVideo;
import com.veeo.common.exception.BaseException;
import com.veeo.common.util.JwtUtils;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.user.client.UserClient;
import com.veeo.video.biz.VideoBiz;
import com.veeo.video.config.BizContext;
import com.veeo.video.constant.RedisConstant;
import com.veeo.video.service.TypeService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/veeo/index")
public class IndexController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private  UserClient userClient;

    @Resource
    private TypeService typeService;

    @Resource
    private VideoBiz videoBiz;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;





    /**
     * 兴趣推送视频
     */
    @GetMapping("/pushVideos")
    public Result<Collection<Video>> pushVideos(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        BizContext bizContext = BizContext.create();
        bizContext.setUserId(userId);
        videoBiz.pushVideo(bizContext);
        return ResultUtil.getSucRet(bizContext.getVideos());
    }

    /**
     * 搜索视频
     */
    @GetMapping("/search")
    public Result<IPage<Video>> searchVideo(@RequestParam(required = false) String searchName,
                              BasePage basePage, HttpServletRequest request) {
        String token = request.getHeader("token");
        BizContext bizContext = BizContext.create();
        bizContext.setSearch(searchName);
        bizContext.setBasePage(basePage);
        bizContext.setUserId(JwtUtils.getUserId(token));

        videoBiz.searchVideo(bizContext);
        return ResultUtil.getSucRet(bizContext.getVideoPage());
    }

    /**
     * 根据视频分类获取
     */
    @GetMapping("/video/type/{typeId}")
    public Result<Collection<Video>> getVideoByTypeId(@PathVariable Long typeId) {
        BizContext bizContext = BizContext.create();
        bizContext.setTypeId(typeId);
        videoBiz.getVideoByType(bizContext);
        return ResultUtil.getSucRet(bizContext.getVideos());
    }

    /**
     * 获取所有分类
     */
    @GetMapping("/types")
    public Result<List<Type>> getTypes(HttpServletRequest request) {
        String token = request.getHeader("token");
        // 如果没有token, 那么就是没有登录. 显示的分类应该是自己订阅的
        if (token == null) {
            return ResultUtil.getSucRet(null);
        }
        List<Type> types = typeService.list(new LambdaQueryWrapper<Type>().select(Type::getIcon, Type::getId, Type::getName).orderByDesc(Type::getSort));
        Result<Collection<Type>> collectionResult = userClient.listSubscribeType(JwtUtils.getUserId(token));
        if (!collectionResult.getState()) {
            throw new BaseException(collectionResult.getMessage());
        }
        Set<Long> set = collectionResult.getData().stream().map(Type::getId).collect(Collectors.toSet());

        types.forEach(type -> {
            type.setUsed(set.contains(type.getId()));
        });
        return ResultUtil.getSucRet(types);
    }

    /**
     * 分享视频
     */
    @PostMapping("/share/{videoId}")
    public Result<Void> share(@PathVariable Long videoId, HttpServletRequest request) {

        String ip = null;
        if (request.getHeader("x-forwarded-for") == null) {
            ip = request.getRemoteAddr();
        } else {
            ip = request.getHeader("x-forwarded-for");
        }

        VideoShare videoShare = new VideoShare();

        videoShare.setVideoId(videoId);
        videoShare.setIp(ip);
        String token = request.getHeader("token");
        if (JwtUtils.checkToken(token)) {
            videoShare.setUserId(JwtUtils.getUserId(token));
        }
        BizContext bizContext = BizContext.create();
        bizContext.setVideoShare(videoShare);
        videoBiz.shareVideo(bizContext);
        return ResultUtil.getSucRet();
    }

    /**
     * 根据id获取视频详情
     */
    @GetMapping("/video/{id}")
    public Result<Video> getVideoById(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader(VeeoHttpConstant.USER_LOGIN_TOKEN);
        Long userId = JwtUtils.getUserId(token);
        BizContext context = BizContext.create();
        context.setUserId(userId);
        context.setVideoId(id);
        videoBiz.getVideoById(context);
        return ResultUtil.getSucRet(context.getVideo());
    }

    /**
     * 获取热度排行榜
     */
    @GetMapping("/video/hot/rank")
    public Result<List<HotVideo>> listHotRank() {
        Set<ZSetOperations.TypedTuple<Object>> zSet = redisTemplate.opsForZSet().reverseRangeWithScores(RedisConstant.HOT_RANK, 0, -1);
        List<HotVideo> hotVideos = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> objectTypedTuple : zSet) {
            HotVideo hotVideo;
            try {
                hotVideo = objectMapper.readValue(objectTypedTuple.getValue().toString(), HotVideo.class);
                hotVideo.setHot((double) objectTypedTuple.getScore().intValue());
                hotVideo.hotFormat();
                hotVideos.add(hotVideo);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return ResultUtil.getSucRet(hotVideos);
    }

    /**
     * 根据视频标签推送相似视频
     */
    @GetMapping("/video/similar")
    public Result<Collection<Video>> pushVideoSimilar(Video video) {
        BizContext bizContext = BizContext.create();
        bizContext.setVideo(video);
        videoBiz.pushSimilarVideo(bizContext);
        return ResultUtil.getSucRet(bizContext.getVideos());
    }

    /**
     * 推送热门视频
     */
    @GetMapping("/video/hot")
    public Result<Collection<Video>> listHotVideo() {
        BizContext context = BizContext.create();
        videoBiz.getHotVideo(context);
        return ResultUtil.getSucRet(context.getVideos());
    }

    /**
     * 根据用户id获取视频
     */
    @GetMapping("/video/user")
    public Result<IPage<Video>> listVideoByUserId(@RequestParam(required = false) Long userId, BasePage basePage, HttpServletRequest request) {

        String token = request.getHeader("token");
        userId = userId == null ? JwtUtils.getUserId(token) : userId;

        BizContext bizContext = BizContext.create();
        bizContext.setUserId(userId);
        bizContext.setBasePage(basePage);
        videoBiz.listByUserIdOpenVideo(bizContext);

        return ResultUtil.getSucRet(bizContext.getVideoPage());
    }

    /**
     * 获取用户搜索记录
     */
    @GetMapping("/search/history")
    public Result<Collection<String>> searchHistory(HttpServletRequest request) {
        String token = request.getHeader("token");
        // 如果没有登录, 肯定无法根据token查询
        // todo 能不能在本地保存一份搜索记录呢, 也比较合理
        if (token == null) {
            return ResultUtil.getSucRet(null);
        }
        return userClient.searchHistory(JwtUtils.getUserId(token));
    }

    /**
     * 删除搜索记录
     */
    @DeleteMapping("/search/history")
    public Result<Void> deleteSearchHistory(HttpServletRequest request) {
        String token = request.getHeader("token");
        userClient.deleteSearchHistory(JwtUtils.getUserId(token));
        return ResultUtil.getSucRet();
    }
}
