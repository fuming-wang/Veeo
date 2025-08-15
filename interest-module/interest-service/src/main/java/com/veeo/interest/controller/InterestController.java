package com.veeo.interest.controller;

import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.UserModel;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.interest.client.InterestClient;
import com.veeo.interest.service.InterestPushService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/interest")
public class InterestController implements InterestClient {

    @Resource
    private InterestPushService interestPushService;


    @Override
    public void pushSystemStockIn(Video video) {
        interestPushService.pushSystemStockIn(video);
    }

    @Override
    public void pushSystemTypeStockIn(Video video) {
        interestPushService.pushSystemTypeStockIn(video);
    }

    @Override
    public Result<Collection<Long>> listVideoIdByTypeId(Long typeId) {

        return ResultUtil.getSucRet(interestPushService.listVideoIdByTypeId(typeId));
    }

    @Override
    public void deleteSystemStockIn(Video video) {

        interestPushService.deleteSystemStockIn(video);
    }

    @Override
    public void initUserModel(Long userId, List<String> labels) {

        interestPushService.initUserModel(userId, labels);
    }

    @Override
    public void updateUserModel(UserModel userModel) {

        interestPushService.updateUserModel(userModel);
    }

    @Override
    public Result<Collection<Long>> listVideoIdByUserModel(User user) {

        return ResultUtil.getSucRet(interestPushService.listVideoIdByUserModel(user));

    }

    @Override
    public Result<Collection<Long>> listVideoIdByUserModel() {
        return ResultUtil.getSucRet(interestPushService.listVideoIdByUserModel(null));
    }

    @Override
    public Result<Collection<Long>> listVideoIdByLabels(List<String> labelNames) {

        return ResultUtil.getSucRet(interestPushService.listVideoIdByLabels(labelNames));
    }

    @Override
    public void deleteSystemTypeStockIn(Video video) {

        interestPushService.deleteSystemTypeStockIn(video);

    }
}
