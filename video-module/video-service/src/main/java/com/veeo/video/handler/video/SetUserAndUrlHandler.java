package com.veeo.video.handler.video;

import com.veeo.common.entity.File;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Video;
import com.veeo.common.entity.vo.UserVO;
import com.veeo.common.util.Result;
import com.veeo.file.client.FileClient;
import com.veeo.user.client.UserClient;
import com.veeo.video.config.BizContext;
import com.veeo.video.handler.BizHandler;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName addMessageToVideoHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/24 22:07
 * @Version 1.0.0
 */
@Service
public class SetUserAndUrlHandler implements BizHandler {

    @Resource
    private FileClient fileClient;

    @Resource
    private UserClient userClient;

    @Override
    public void handle(BizContext context) {
        Collection<Video> videos = context.getVideos();
        // todo 对videos进分片异步处理
        if (ObjectUtils.isEmpty(videos)) {
            return;
        }
        Set<Long> userIds = new HashSet<>();
        List<Long> fileIds = new ArrayList<>();
        videos.forEach(video -> {
            userIds.add(video.getUserId());
            fileIds.add(video.getUrl());
            fileIds.add(video.getCover());
        });
        Result<Collection<File>> collectionResult = fileClient.listByIds(fileIds);
        if (!collectionResult.getState()) {
            throw new RuntimeException(collectionResult.getMessage());
        }
        Map<Long, File> fileMap = collectionResult.getData().stream().collect(Collectors.toMap(File::getId, Function.identity()));
        Result<List<User>> listResult = userClient.list(userIds);
        if (!listResult.getState()) {
            throw new RuntimeException(listResult.getMessage());
        }
        Map<Long, User> userMap = listResult.getData().stream().collect(Collectors.toMap(User::getId, Function.identity()));

        videos.forEach(video -> {
            UserVO userVO = new UserVO();
            User user = userMap.get(video.getUserId());
            userVO.setId(video.getUserId());
            userVO.setNickName(user.getNickName());
            userVO.setDescription(user.getDescription());
            userVO.setSex(user.getSex());
            video.setUser(userVO);
            File file = fileMap.get(video.getUrl());
            video.setVideoType(file.getFormat());
        });
    }
}
