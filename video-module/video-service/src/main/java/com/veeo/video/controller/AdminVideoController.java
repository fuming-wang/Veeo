package com.veeo.video.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.veeo.common.authority.Authority;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.entity.vo.VideoStatistics;
import com.veeo.common.exception.BaseException;
import com.veeo.common.holder.UserHolder;
import com.veeo.common.query.QueryCondition;
import com.veeo.common.query.QueryDTO;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.user.client.UserClient;
import com.veeo.video.biz.VideoBiz;
import com.veeo.video.config.BizContext;
import com.veeo.video.constant.AuditStatus;
import com.veeo.video.service.TypeService;
import com.veeo.video.service.VideoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/admin/video")
public class AdminVideoController {
    @Resource
    private VideoService videoService;
    @Resource
    private UserClient userClient;
    @Resource
    private TypeService typeService;

    @Resource
    private VideoBiz videoBiz;


    @GetMapping("/{id}")
    @Authority("admin:video:get")
    public Result<Video> get(@PathVariable Long id) {
        BizContext context = BizContext.create();
        context.setUserId(id);
        videoBiz.getVideoById(context);
        return ResultUtil.getSucRet(context.getVideo());
    }


    @GetMapping("/page")
    @Authority("admin:video:page")
    public Result<List<Video>> list(BasePage basePage,
                       @RequestParam(required = false) String YV,
                       @RequestParam(required = false) String title) {

        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(!ObjectUtils.isEmpty(YV), Video::getYv, YV)
                .like(!ObjectUtils.isEmpty(title), Video::getTitle, title);

        IPage<Video> page = videoService.page(basePage.videoPage(), wrapper);

        List<Video> records = page.getRecords();
        if (ObjectUtils.isEmpty(records)) {
            return ResultUtil.getSucRet();
        }

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
        Result<List<User>> listResult = userClient.list(queryDTO);
        if (!listResult.getState()) {
            throw new BaseException(listResult.getMessage());
        }
        List<User> resultList = listResult.getData();

        Map<Long, String> userMap = resultList.stream().collect(Collectors.toMap(User::getId, User::getNickName));
        Map<Long, String> typeMap = typeService.listByIds(typeIds).stream().collect(Collectors.toMap(Type::getId, Type::getName));
        for (Video video : records) {
            video.setAuditStateName(AuditStatus.getName(video.getAuditStatus()));
            video.setUserName(userMap.get(video.getUserId()));
            video.setOpenName(video.getOpen() ? "私密" : "公开");
            video.setTypeName(typeMap.get(video.getTypeId()));
        }
        return ResultUtil.getSucRet(records).count(page.getTotal());
    }

    /**
     * 删除视频
     *
     * @param id 视频id
     * @return R
     */
    @DeleteMapping("/{id}")
    @Authority("admin:video:delete")
    public Result<Void> delete(@PathVariable Long id) {
        BizContext context = BizContext.create();
        context.setUserId(UserHolder.get());
        context.setVideoId(id);
        videoBiz.deleteVideoById(context);
        return ResultUtil.getSucRet("删除成功");
    }

    /**
     * 放行视频
     */
    @PostMapping("/audit")
    @Authority("admin:video:audit")
    public Result<Void> audit(@RequestBody Video video) {
        BizContext context = BizContext.create();
        context.setVideo(video);
        videoBiz.auditVideo(context);
        return ResultUtil.getSucRet("审核放行");
    }

    /**
     * 下架视频
     */
    @PostMapping("/violations/{id}")
    @Authority("admin:video:violations")
    public Result<Void> Violations(@PathVariable Long id) {
        BizContext context = BizContext.create();
        context.setVideoId(id);
        videoBiz.banVideo(context);
        return ResultUtil.getSucRet("下架成功");
    }


    /**
     * 视频数据统计
     *
     * @return x
     */
    @GetMapping("/statistics")
    @Authority("admin:video:statistics")
    public Result<VideoStatistics> show() {
        VideoStatistics videoStatistics = new VideoStatistics();
        int allCount = (int) videoService.count(new LambdaQueryWrapper<>());
        int processCount = (int) videoService.count(new LambdaQueryWrapper<Video>().eq(Video::getAuditStatus, AuditStatus.PROCESS));
        int successCount = (int) videoService.count(new LambdaQueryWrapper<Video>().eq(Video::getAuditStatus, AuditStatus.SUCCESS));
        int passCount = (int) videoService.count(new LambdaQueryWrapper<Video>().eq(Video::getAuditStatus, AuditStatus.PASS));
        videoStatistics.setAllCount(allCount);
        videoStatistics.setPassCount(passCount);
        videoStatistics.setSuccessCount(successCount);
        videoStatistics.setProcessCount(processCount);
        return ResultUtil.getSucRet(videoStatistics);
    }
}
