package com.veeo.video.handler.video;

import com.veeo.common.entity.video.Type;
import com.veeo.common.exception.BaseException;
import com.veeo.common.util.Result;
import com.veeo.interest.client.InterestClient;
import com.veeo.video.config.BizContext;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.TypeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.Collections;

/**
 * @ClassName getVideoByTypeIdHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/25 21:15
 * @Version 1.0.0
 */
@Service
public class GetVideoByTypeIdHandler implements BizHandler {

    @Resource
    private TypeService typeService;

    @Resource
    private InterestClient interestClient;

    @Override
    public void handle(BizContext context) {
        Long typeId = context.getTypeId();
        if (typeId == null) {
            context.setVideoIds(Collections.emptyList());
        }
        // TODO 少量查询, 后续优化
        Type type = typeService.getById(typeId);
        if (type == null) {
            context.setVideoIds(Collections.emptyList());
        }

        Result<Collection<Long>> videoIdsResult = interestClient.listVideoIdByTypeId(typeId);
        if (videoIdsResult.isFailed()) {
            throw new BaseException(videoIdsResult.getMessage());
        }
        Collection<Long> videoIds = videoIdsResult.getData();
        if (ObjectUtils.isEmpty(videoIds)) {
            context.setVideoIds(Collections.emptyList());
        }
        context.setVideoIds(videoIds);

    }
}
