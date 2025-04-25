package com.veeo.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeo.common.config.LocalCache;
import com.veeo.common.config.QiNiuConfig;
import com.veeo.common.constant.AuditStatus;
import com.veeo.common.constant.RedisConstant;
import com.veeo.common.entity.File;
import com.veeo.common.entity.task.VideoTask;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.video.VideoShare;
import com.veeo.common.entity.video.VideoStar;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.entity.vo.HotVideo;
import com.veeo.common.entity.vo.UserModel;
import com.veeo.common.entity.vo.UserVO;
import com.veeo.common.exception.BaseException;
import com.veeo.common.holder.UserHolder;
import com.veeo.common.util.FileUtil;

import com.veeo.common.util.RedisCacheUtil;
import com.veeo.file.api.FileClient;
import com.veeo.interest.api.FeedClient;
import com.veeo.interest.api.InterestClient;
import com.veeo.user.api.FollowClient;
import com.veeo.user.api.UserClient;
import com.veeo.video.mapper.VideoMapper;

import com.veeo.video.service.*;
import com.veeo.video.service.audit.VideoPublishAuditServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;


import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {


    @Autowired
    private TypeService typeService;

    @Autowired
    private InterestClient interestClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private VideoStarService videoStarService;

    @Autowired
    private VideoShareService videoShareService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private FavoritesService favoritesService;

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoPublishAuditServiceImpl videoPublishAuditService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private FollowClient followClient;

    @Autowired
    private FeedClient feedClient;

    @Autowired
    private FileClient fileClient;


    ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public Video getVideoById(Long videoId, Long userId) {
        Video video = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, videoId));

        if (video == null) throw new BaseException("指定视频不存在");

        // 私密则返回为空
        if (video.getOpen()) return new Video();

        setUserVoAndUrl(Collections.singleton(video));

        // 当前视频用户自己是否有收藏/点赞过等信息
        // 这里需要优化 如果这里开线程获取则系统g了(因为这里的场景不适合) -> 请求数很多
        // 正确做法: 视频存储在redis中，点赞收藏等行为异步放入DB, 定时任务扫描DB中不重要更新redis
        video.setStart(videoStarService.starState(videoId, userId));
        video.setFavorites(favoritesService.favoritesState(videoId, userId));
        video.setFollow(followClient.isFollows(video.getUserId(), userId));
        return video;
    }


    @Override
    public void publishVideo(Video video) {

        Long userId = UserHolder.get();
        Video oldVideo = null;
        // 不允许修改视频
        final Long videoId = video.getId();
        if (videoId != null) {
            // url不能一致
            oldVideo = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, videoId).eq(Video::getUserId, userId));
            if (!(video.buildVideoUrl()).equals(oldVideo.buildVideoUrl()) || !(video.buildCoverUrl().equals(oldVideo.buildCoverUrl()))) {
                throw new BaseException("不能更换视频源,只能修改视频信息");
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
        video.setUserId(userId);

        boolean isAdd = videoId == null ? true : false;

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
                video.setCover(fileClient.generatePhoto(video.getUrl(), userId));
            }

            video.setYv("YV" + UUID.randomUUID().toString().replace("-", "").substring(8));
        }

        // 填充视频时长 (若上次发布视频不存在Duration则会尝试获取)
        if (isAdd || !StringUtils.hasLength(oldVideo.getDuration())) {
            final String uuid = UUID.randomUUID().toString();
            LocalCache.put(uuid, true);
            try {
                Long url = video.getUrl();
                if (url == null || url == 0) url = oldVideo.getUrl();
                final String fileKey = fileClient.getById(url).getFileKey();
                final String duration = FileUtil.getVideoDuration(QiNiuConfig.CNAME + "/" + fileKey + "?uuid=" + uuid);
                video.setDuration(duration);
            } finally {
                LocalCache.rem(uuid);
            }
        }

        this.saveOrUpdate(video);

        VideoTask videoTask = new VideoTask();
        videoTask.setOldVideo(video);
        videoTask.setVideo(video);
        videoTask.setIsAdd(isAdd);
        videoTask.setOldState(isAdd ? true : video.getOpen());
        videoTask.setNewState(true);
        videoPublishAuditService.audit(videoTask, false);
    }


    @Override
    public void deleteVideo(Long id) {

        if (id == null) {
            throw new BaseException("删除指定的视频不存在");
        }

        final Long userId = UserHolder.get();
        final Video video = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, id).eq(Video::getUserId, userId));
        if (video == null) {
            throw new BaseException("删除指定的视频不存在");
        }
        final boolean b = removeById(id);
        if (b) {
            // 解耦
            new Thread(() -> {
                // 删除分享量 点赞量
                videoShareService.remove(new LambdaQueryWrapper<VideoShare>().eq(VideoShare::getVideoId, id).eq(VideoShare::getUserId, userId));
                videoStarService.remove(new LambdaQueryWrapper<VideoStar>().eq(VideoStar::getVideoId, id).eq(VideoStar::getUserId, userId));
                interestClient.deleteSystemStockIn(video);
                interestClient.deleteSystemTypeStockIn(video);
            }).start();
        }
    }

    @Override
    public Collection<Video> pushVideos(Long userId) {
        // todo
        User user = null;
        if (userId != null) {
            user = userClient.getById(userId);
        }
        log.warn("pushVideos user: {}", user);
        // 如果有user为空呢？
        Collection<Long> videoIds;
        if(user == null) {
            log.warn("user is null, 游客登录了");
            videoIds = interestClient.listVideoIdByUserModel();
        }else {
            log.warn("user is not null, 用户登录");
            videoIds = interestClient.listVideoIdByUserModel(user);
        }

        Collection<Video> videos = new ArrayList<>();

        if (ObjectUtils.isEmpty(videoIds)) {
            videoIds = list(new LambdaQueryWrapper<Video>().orderByDesc(Video::getGmtCreated)).stream().map(Video::getId).collect(Collectors.toList());
            videoIds = new HashSet<>(videoIds).stream().limit(10).collect(Collectors.toList());
        }
        videos = listByIds(videoIds);
        setUserVoAndUrl(videos);
        return videos;
    }

    @Override
    public Collection<Video> getVideoByTypeId(Long typeId) {
        if (typeId == null) return Collections.EMPTY_LIST;
        Type type = typeService.getById(typeId);
        if (type == null) return Collections.EMPTY_LIST;

        Collection<Long> videoIds = interestClient.listVideoIdByTypeId(typeId);
        if (ObjectUtils.isEmpty(videoIds)) {
            return Collections.EMPTY_LIST;
        }
        final Collection<Video> videos = listByIds(videoIds);

        setUserVoAndUrl(videos);
        return videos;
    }

    @Override
    public IPage<Video> searchVideo(String search, BasePage basePage, Long userId) {
        final IPage p = basePage.page();
        // 如果带YV则精准搜该视频
        final LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getAuditStatus, AuditStatus.SUCCESS);
        if (search.contains("YV")) {
            wrapper.eq(Video::getYv, search);
        } else {
            wrapper.like(!ObjectUtils.isEmpty(search), Video::getTitle, search);
        }
        IPage<Video> page = this.page(p, wrapper);

        final List<Video> videos = page.getRecords();
        setUserVoAndUrl(videos);

        userClient.addSearchHistory(userId, search);
        return page;
    }

    @Override
    public void auditProcess(Video video) {
        // 放行后
        updateById(video);
        interestClient.pushSystemStockIn(video);
        interestClient.pushSystemTypeStockIn(video);
        // 推送该视频博主的发件箱
        feedClient.pushInBoxFeed(video.getUserId(), video.getId(), video.getGmtCreated().getTime());
    }

    @Override
    public boolean startVideo(Long videoId) {
        final Video video = getById(videoId);
        if (video == null) throw new BaseException("指定视频不存在");

        final VideoStar videoStar = new VideoStar();
        videoStar.setVideoId(videoId);
        videoStar.setUserId(UserHolder.get());
        final boolean result = videoStarService.starVideo(videoStar);
        updateStar(video, result ? 1L : -1L);
        // 获取标签
        final List<String> labels = video.buildLabel();

        final UserModel userModel = UserModel.buildUserModel(labels, videoId, 1.0);
        interestClient.updateUserModel(userModel);

        return result;
    }

    @Override
    public boolean favoritesVideo(Long fId, Long vId) {
        final Video video = getById(vId);
        if (video == null) {
            throw new BaseException("指定视频不存在");
        }
        final boolean favorites = favoritesService.favorites(fId, vId);
        updateFavorites(video, favorites ? 1L : -1L);

        final List<String> labels = video.buildLabel();

        final UserModel userModel = UserModel.buildUserModel(labels, vId, 2.0);
        interestClient.updateUserModel(userModel);

        return favorites;
    }

    @Override
    public boolean shareVideo(VideoShare videoShare) {
        final Video video = getById(videoShare.getVideoId());
        if (video == null) throw new BaseException("指定视频不存在");
        final boolean result = videoShareService.share(videoShare);
        updateShare(video, result ? 1L : 0L);
        return result;
    }

    @Override
    @Async
    public void historyVideo(Long videoId, Long userId) {
        String key = RedisConstant.HISTORY_VIDEO + videoId + ":" + userId;
        final Object o = redisCacheUtil.get(key);
        if (o == null) {
            redisCacheUtil.set(key, videoId, RedisConstant.HISTORY_TIME);
            final Video video = getById(videoId);
            video.setUser(userClient.getInfo(video.getUserId()));
            video.setTypeName(typeService.getById(video.getTypeId()).getName());
            redisCacheUtil.zadd(RedisConstant.USER_HISTORY_VIDEO + userId, new Date().getTime(), video, RedisConstant.HISTORY_TIME);
            updateHistory(video, 1L);
        }
    }

    @Override
    public LinkedHashMap<String, List<Video>> getHistory(BasePage basePage) {

        final Long userId = UserHolder.get();
        String key = RedisConstant.USER_HISTORY_VIDEO + userId;
        final Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisCacheUtil.zSetGetByPage(key, basePage.getPage(), basePage.getLimit());
        if (ObjectUtils.isEmpty(typedTuples)) {
            return new LinkedHashMap<>();
        }
        List<Video> temp = new ArrayList<>();
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final LinkedHashMap<String, List<Video>> result = new LinkedHashMap<>();
        for (ZSetOperations.TypedTuple<Object> typedTuple : typedTuples) {
            final Date date = new Date(typedTuple.getScore().longValue());
            final String format = simpleDateFormat.format(date);
            if (!result.containsKey(format)) {
                result.put(format, new ArrayList<>());
            }
            final Video video = (Video) typedTuple.getValue();
            result.get(format).add(video);
            temp.add(video);
        }
        setUserVoAndUrl(temp);

        return result;
    }

    @Override
    public Collection<Video> listVideoByFavorites(Long favoritesId) {
        final List<Long> videoIds = favoritesService.listVideoIds(favoritesId, UserHolder.get());
        if (ObjectUtils.isEmpty(videoIds)) {
            return Collections.EMPTY_LIST;
        }
        final Collection<Video> videos = listByIds(videoIds);
        setUserVoAndUrl(videos);
        return videos;
    }


    @Override
    public List<HotVideo> hotRank() {
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
                e.printStackTrace();
            }
        }
        return hotVideos;
    }



    @Override
    public Collection<Video> listSimilarVideo(Video video) {

        if (ObjectUtils.isEmpty(video) || ObjectUtils.isEmpty(video.getLabelNames())) return Collections.EMPTY_LIST;

        List<String> labels = video.buildLabel();
        // FIXME bug
        List<String> labelNames = new ArrayList<>(labels);
        List<Long> similarVideoIds = (List<Long>) interestClient.listVideoIdByLabels(labelNames);
        Set<Long> videoIds = new HashSet<>(similarVideoIds);
        Collection<Video> videos = new ArrayList<>();
        // 去重
        videoIds.remove(video.getId());

        if (!ObjectUtils.isEmpty(videoIds)) {
            videos = listByIds(videoIds);
            setUserVoAndUrl(videos);
        }

        return videos;
    }

    @Override
    public IPage<Video> listByUserIdOpenVideo(Long userId, BasePage basePage) {
        if (userId == null) {
            return new Page<>();
        }
        IPage<Video> page = page(basePage.page(), new LambdaQueryWrapper<Video>().eq(Video::getUserId, userId).eq(Video::getAuditStatus, AuditStatus.SUCCESS).orderByDesc(Video::getGmtCreated));
        List<Video> videos = page.getRecords();
        setUserVoAndUrl(videos);
        return page;
    }

    @Override
    public String getAuditQueueState() {
        return videoPublishAuditService.getAuditQueueState() ? "快速" : "慢速";
    }


    @Override
    public List<Video> selectNDaysAgeVideo(long id, int days, int limit) {
        return videoMapper.selectNDaysAgeVideo(id, days, limit);
    }

    @Override
    public Collection<Video> listHotVideo() {

        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DATE);

        HashMap<String, Integer> map = new HashMap<>();
        // 优先推送今日的
        map.put(RedisConstant.HOT_VIDEO + today, 10);
        map.put(RedisConstant.HOT_VIDEO + (today - 1), 3);
        map.put(RedisConstant.HOT_VIDEO + (today - 2), 2);

        // 游客不用记录
        // 获取今天日期
        List<Long> hotVideoIds = redisCacheUtil.pipeline(connection -> {
            map.forEach((k, v) -> {
                connection.sRandMember(k.getBytes(), v);
            });
            return null;
        });
        if (ObjectUtils.isEmpty(hotVideoIds)) {
            return Collections.EMPTY_LIST;
        }
        ArrayList<Long> videoIds = new ArrayList<>();
        // 会返回结果有null，做下校验
        for (Object videoId : hotVideoIds) {
            if (!ObjectUtils.isEmpty(videoId)) {
                videoIds.addAll((List) videoId);
            }
        }
        if (ObjectUtils.isEmpty(videoIds)){
           return Collections.EMPTY_LIST;

        }
        Collection<Video> videos = listByIds(videoIds);
        // 和浏览记录做交集? 不需要做交集，热门视频和兴趣推送不一样
        setUserVoAndUrl(videos);
        return videos;
    }

    @Override
    public Collection<Video> followFeed(Long userId, Long lastTime) {

        // 是否存在
        /*
         * 我们以时间戳作为分数，分页通常是基于上一次返回的最后一个元素的时间戳来做条件的，如果不跳过上一次的边界值，就会出现重复数据。
         */
        Set<Long> set = redisTemplate.opsForZSet().reverseRangeByScore(RedisConstant.IN_FOLLOW + userId,
                        0, lastTime == null ? new Date().getTime() : lastTime, lastTime == null ? 0 : 1, 5);

        if (ObjectUtils.isEmpty(set)) {
            // 可能只是缓存中没有了,缓存只存储7天内的关注视频,继续往后查看关注的用户太少了,不做考虑 - feed流必然会产生的问题
            return Collections.EMPTY_LIST;
        }

        // 这里不会按照时间排序，需要手动排序
        Collection<Video> videos = list(new LambdaQueryWrapper<Video>().in(Video::getId, set).orderByDesc(Video::getGmtCreated));

        setUserVoAndUrl(videos);

        return videos;
    }

    @Override
    public void initFollowFeed(Long userId) {
        // 获取所有关注的人
        if (userId == null) {
            log.error("initFollowFeed userId is null");
        }
        Collection<Long> followIds = followClient.getFollow(userId);
        feedClient.initFollowFeed(userId, followIds);
    }

    @Override
    public IPage<Video> listByUserIdVideo(BasePage basePage, Long userId) {

        IPage page = page(basePage.page(), new LambdaQueryWrapper<Video>().eq(Video::getUserId, userId).orderByDesc(Video::getGmtCreated));
        setUserVoAndUrl(page.getRecords());
        return page;
    }

    @Override
    public Collection<Long> listVideoIdByUserId(Long userId) {

        List<Long> ids = list(new LambdaQueryWrapper<Video>().eq(Video::getUserId, userId).eq(Video::getOpen, 0).select(Video::getId))
                .stream().map(Video::getId).collect(Collectors.toList());
        return ids;
    }

    @Override
    public void violations(Long id) {
        Video video = getById(id);
        Type type = typeService.getById(video.getTypeId());
        video.setLabelNames(type.getLabelNames());
        // 修改视频信息
        video.setOpen(true);
        video.setMsg("该视频违反了Veeo平台的规则,已被下架私密");
        video.setAuditStatus(AuditStatus.PASS);
        // 删除分类中的视频
        interestClient.deleteSystemTypeStockIn(video);
        // 删除标签中的视频
        interestClient.deleteSystemStockIn(video);
        // 获取视频发布者id,删除对应的发件箱
        Long userId = video.getUserId();
        redisTemplate.opsForZSet().remove(RedisConstant.OUT_FOLLOW + userId, id);

        // 获取视频发布者粉丝，删除对应的收件箱
        Collection<Long> fansIds = followClient.getFans(userId, null);
        feedClient.deleteInBoxFeed(userId, Collections.singletonList(id));
        feedClient.deleteOutBoxFeed(userId, fansIds, id);

        // 热门视频以及热度排行榜视频
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DATE);
        final Long videoId = video.getId();
        // 尝试去找到删除
        redisTemplate.opsForSet().remove(RedisConstant.HOT_VIDEO + today, videoId);
        redisTemplate.opsForSet().remove(RedisConstant.HOT_VIDEO + (today - 1), videoId);
        redisTemplate.opsForSet().remove(RedisConstant.HOT_VIDEO + (today - 2), videoId);
        redisTemplate.opsForZSet().remove(RedisConstant.HOT_RANK, videoId);
        // 修改视频
        updateById(video);
    }

    public void setUserVoAndUrl(Collection<Video> videos) {
        if (!ObjectUtils.isEmpty(videos)) {
            Set<Long> userIds = new HashSet<>();
            List<Long> fileIds = new ArrayList<>();
            for (Video video : videos) {
                userIds.add(video.getUserId());
                fileIds.add(video.getUrl());
                fileIds.add(video.getCover());
            }
            Map<Long, File> fileMap = fileClient.listByIds(fileIds).stream().collect(Collectors.toMap(File::getId, Function.identity()));
            Map<Long, User> userMap = userClient.list(userIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));
            log.warn("userIds:{} fileIds:{}", userIds, fileIds);
            log.warn("userMap:{}", userMap);
            log.warn("fileMap:{}", fileMap);
            for (Video video : videos) {
                UserVO userVO = new UserVO();
                User user = userMap.get(video.getUserId());
                userVO.setId(video.getUserId());
                userVO.setNickName(user.getNickName());
                userVO.setDescription(user.getDescription());
                userVO.setSex(user.getSex());
                video.setUser(userVO);
                File file = fileMap.get(video.getUrl());
                video.setVideoType(file.getFormat());
            }
        }

    }


    /**
     * 点赞数
     *
     * @param video
     */
    public void updateStar(Video video, Long value) {
        final UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("start_count = start_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getStartCount, video.getStartCount());
        update(video, updateWrapper);
    }

    /**
     * 分享数
     *
     * @param video
     */
    public void updateShare(Video video, Long value) {
        UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("share_count = share_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getShareCount, video.getShareCount());
        update(video, updateWrapper);
    }

    /**
     * 浏览量
     *
     * @param video
     */
    public void updateHistory(Video video, Long value) {
        UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("history_count = history_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getHistoryCount, video.getHistoryCount());
        update(video, updateWrapper);
    }

    public void updateFavorites(Video video, Long value) {
        final UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("favorites_count = favorites_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getFavoritesCount, video.getFavoritesCount());
        update(video, updateWrapper);
    }


}
