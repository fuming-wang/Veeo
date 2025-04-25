package com.veeo.video.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.veeo.common.authority.Authority;
import com.veeo.common.constant.AuditStatus;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.entity.vo.VideoStatistics;
import com.veeo.common.query.QueryCondition;
import com.veeo.common.query.QueryDTO;
import com.veeo.common.util.R;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.user.api.UserClient;
import com.veeo.video.service.TypeService;
import com.veeo.video.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/admin/video")
public class AdminVideoController {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    private final VideoService videoService;

    private final UserClient userClient;

    private final TypeService typeService;

    public AdminVideoController(VideoService videoService, UserClient userClient, TypeService typeService) {
        this.videoService = videoService;
        this.userClient = userClient;
        this.typeService = typeService;
    }

    @GetMapping("/{id}")
    @Authority("admin:video:get")
    public R get(@PathVariable Long id){
        return R.ok().data(videoService.getVideoById(id,null));
    }


    @GetMapping("/page")
    @Authority("admin:video:page")
    public R list(BasePage basePage, @RequestParam(required = false) String YV, @RequestParam(required = false) String title){

        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(!ObjectUtils.isEmpty(YV),Video::getYv,YV).like(!ObjectUtils.isEmpty(title),Video::getTitle,title);

        IPage<Video> page = videoService.page(basePage.page(), wrapper);

        List<Video> records = page.getRecords();
        if (ObjectUtils.isEmpty(records)) return R.ok();

        List<Long> userIds = new ArrayList<>();
        List<Long> typeIds = new ArrayList<>();
        for (Video video : records) {
            userIds.add(video.getUserId());
            typeIds.add(video.getTypeId());
        }

        QueryCondition selectCondition = QueryCondition.select("id", "nickName");
        QueryCondition condition = QueryCondition.and().in("id", userIds);
        QueryDTO queryDTO = new QueryDTO();
        queryDTO.addConditions(selectCondition, condition);

        List<User> resultList = userClient.list(queryDTO);
        log.info("resultList:{}", resultList);
        Map<Long, String> userMap = resultList
                .stream()
                .collect(Collectors.toMap(User::getId, User::getNickName));
        Map<Long, String> typeMap = typeService.listByIds(typeIds).stream().collect(Collectors.toMap(Type::getId, Type::getName));

        for (Video video : records) {
            video.setAuditStateName(AuditStatus.getName(video.getAuditStatus()));
            video.setUserName(userMap.get(video.getUserId()));
            video.setOpenName(video.getOpen() ? "私密" : "公开");
            video.setTypeName(typeMap.get(video.getTypeId()));
        }
        return R.ok().data(records).count(page.getTotal());
    }

    /**
     * 删除视频
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @Authority("admin:video:delete")
    public R delete(@PathVariable Long id){
        videoService.deleteVideo(id);
        return R.ok().message("删除成功");
    }

    /**
     * 放行视频
     * @param video
     * @return
     */
    @PostMapping("/audit")
    @Authority("admin:video:audit")
    public R audit(@RequestBody Video video){
        videoService.auditProcess(video);
        return R.ok().message("审核放行");
    }

    /**
     * 下架视频
     * @param id
     * @return
     */
    @PostMapping("/violations/{id}")
    @Authority("admin:video:violations")
    public R Violations(@PathVariable Long id){
        videoService.violations(id);
        return R.ok().message("下架成功");
    }


    /**
     * 视频数据统计
     * @return
     */
    @GetMapping("/statistics")
    @Authority("admin:video:statistics")
    public R show(){
        VideoStatistics videoStatistics = new VideoStatistics();
        int allCount = (int) videoService.count(new LambdaQueryWrapper<Video>());
        int processCount = (int) videoService.count(new LambdaQueryWrapper<Video>().eq(Video::getAuditStatus, AuditStatus.PROCESS));
        int successCount = (int) videoService.count(new LambdaQueryWrapper<Video>().eq(Video::getAuditStatus, AuditStatus.SUCCESS));
        int passCount = (int) videoService.count(new LambdaQueryWrapper<Video>().eq(Video::getAuditStatus, AuditStatus.PASS));
        videoStatistics.setAllCount(allCount);
        videoStatistics.setPassCount(passCount);
        videoStatistics.setSuccessCount(successCount);
        videoStatistics.setProcessCount(processCount);

        return R.ok().data(videoStatistics);
    }
}
