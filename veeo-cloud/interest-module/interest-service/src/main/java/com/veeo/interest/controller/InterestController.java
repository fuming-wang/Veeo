package com.veeo.interest.controller;

import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.UserModel;
import com.veeo.interest.api.InterestClient;
import com.veeo.interest.service.InterestPushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/interest")
public class InterestController implements InterestClient {

    private final InterestPushService interestPushService;

    public InterestController(InterestPushService interestPushService) {
        this.interestPushService = interestPushService;
    }

    @RequestMapping("/test")
    public String test(){
        return "test";
    }

    @Override
    public void pushSystemStockIn(Video video) {
        if (video == null) {
            log.error("video is null");
        }
        interestPushService.pushSystemStockIn(video);
    }

    @Override
    public void pushSystemTypeStockIn(Video video) {
        if (video == null) {
            log.error("video is null");
        }
        interestPushService.pushSystemTypeStockIn(video);
    }

    @Override
    public Collection<Long> listVideoIdByTypeId(Long typeId) {
        if (typeId == null) {
            log.error("typeId is null");
        }
        return interestPushService.listVideoIdByTypeId(typeId);
    }

    @Override
    public void deleteSystemStockIn(Video video) {
        if (video == null) {
            log.error("video is null");
        }
        interestPushService.deleteSystemStockIn(video);
    }

    @Override
    public void initUserModel(Long userId, List<String> labels) {
        if (userId == null || labels == null) {
            log.error("userId and labels is null");
        }
        interestPushService.initUserModel(userId, labels);
    }

    @Override
    public void updateUserModel(UserModel userModel) {
        if (userModel == null) {
            log.error("userModel is null");
        }
        interestPushService.updateUserModel(userModel);
    }

    @Override
    public Collection<Long> listVideoIdByUserModel(User user) {
        if (user == null) {
            log.error("user is null");
        }
        return interestPushService.listVideoIdByUserModel(user);

    }

    @Override
    public Collection<Long> listVideoIdByUserModel() {
        return interestPushService.listVideoIdByUserModel(null);
    }

    @Override
    public Collection<Long> listVideoIdByLabels(List<String> labelNames) {
        if (labelNames == null) {
            log.error("labelNames is null");
        }
        return interestPushService.listVideoIdByLabels(labelNames);
    }

    @Override
    public void deleteSystemTypeStockIn(Video video) {
        if (video == null) {
            log.error("video is null");
        }
        interestPushService.deleteSystemTypeStockIn(video);

    }
}
