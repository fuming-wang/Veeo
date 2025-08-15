package com.veeo.video.handler.video;

import com.veeo.common.entity.video.Video;
import com.veeo.common.exception.BaseException;
import com.veeo.common.util.Result;
import com.veeo.interest.client.InterestClient;
import com.veeo.video.config.BizContext;
import com.veeo.video.handler.BizHandler;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * @ClassName ListSimilarVideoId
 * @Description
 * @Author wangfuming
 * @Date 2025/7/26 12:09
 * @Version 1.0.0
 */
@Service
public class ListSimilarVideoIdHandler implements BizHandler {

    @Resource
    private InterestClient interestClient;

    @Override
    public void handle(BizContext context) {
        Video video = context.getVideo();

        // 为空不推荐，但是责任链设计认为是弱依赖
        if (ObjectUtils.isEmpty(video) || ObjectUtils.isEmpty(video.getLabelNames())) {
            context.setVideoIds(Collections.emptyList());
            return;
        }

        List<String> labels = video.buildLabel();
        // FIXME bug
        List<String> labelNames = new ArrayList<>(labels);
        Result<Collection<Long>> findLabelIds = interestClient.listVideoIdByLabels(labelNames);
        if (!findLabelIds.getState()) {
            throw new BaseException(findLabelIds.getMessage());
        }
        List<Long> similarVideoIds = (List<Long>) findLabelIds.getData();
        Set<Long> videoIds = new HashSet<>(similarVideoIds);
        Collection<Video> videos = new ArrayList<>();
        // 去重
        videoIds.remove(video.getId());
        context.setVideoIds(videoIds);
    }
}
