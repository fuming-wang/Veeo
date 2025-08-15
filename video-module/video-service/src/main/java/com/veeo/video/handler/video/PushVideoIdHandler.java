package com.veeo.video.handler.video;

import com.veeo.common.entity.user.User;
import com.veeo.common.exception.BaseException;
import com.veeo.common.util.Result;
import com.veeo.interest.client.InterestClient;
import com.veeo.user.client.UserClient;
import com.veeo.video.config.BizContext;
import com.veeo.video.handler.BizHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * @ClassName PushVideoIdHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/26 12:44
 * @Version 1.0.0
 */
@Service
@Slf4j
public class PushVideoIdHandler implements BizHandler {

    @Resource
    private UserClient userClient;

    @Resource
    private InterestClient interestClient;

    @Override
    public void handle(BizContext context) {
        Long userId = context.getUserId();
        User user = null;
        if (userId != null) {
            Result<User> userResult = userClient.getById(userId);
            if (userResult.isFailed()) {
                // 弱依赖不阻断流程
                // todo 设置为弱依赖合理吗
                log.error("[PushVideoIdHandler] request user error, userId:{}", userId);
            } else {
                user = userResult.getData();
            }
        }
        // 如果有user为空呢？
        Result<Collection<Long>> videoIdsResult;
        if (user == null) {
            videoIdsResult = interestClient.listVideoIdByUserModel();
        } else {
            videoIdsResult = interestClient.listVideoIdByUserModel(user);
        }
        if (videoIdsResult.isFailed()) {
            log.error("[PushVideoIdHandler] request videoIds error, userId:{}", userId);
            // 强依赖，阻断流程
            throw new BaseException(videoIdsResult.getMessage());
        }
        Collection<Long> videoIds = videoIdsResult.getData();

//        if (ObjectUtils.isEmpty(videoIds)) {
//            videoIds = list(new LambdaQueryWrapper<Video>().orderByDesc(Video::getGmtCreated)).stream().map(Video::getId).collect(Collectors.toList());
//            videoIds = new HashSet<>(videoIds).stream().limit(10).collect(Collectors.toList());
//        }
        context.setVideoIds(videoIds);
    }
}
