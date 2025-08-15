package com.veeo.video.biz;

import com.veeo.video.config.BizBuilder;
import com.veeo.video.config.BizContext;
import com.veeo.video.handler.BizHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName VideoBiz
 * @Description
 * @Author wangfuming
 * @Date 2025/7/24 22:17
 * @Version 1.0.0
 */
@Service
public class VideoBiz extends BizExecute {

    @Resource
    private BizBuilder bizBuilder;

    private List<BizHandler> getVideoByIdBiz;

    private List<BizHandler> deleteVideoByIdBiz;

    private List<BizHandler> auditVideoBiz;

    private List<BizHandler> startVideoBiz;

    private List<BizHandler> favoriteVideoBiz;

    private List<BizHandler> getVideoByTypeBiz;

    private List<BizHandler> shareVideoBiz;

    private List<BizHandler> searchVideoBiz;

    private List<BizHandler> banVideoBiz;

    private List<BizHandler> listVideoByFavoritesBiz;

    private List<BizHandler> addHistoryVideoBiz;

    private List<BizHandler> getHistoryVideoBiz;

    private List<BizHandler> pushSimilarVideoBiz;

    private List<BizHandler> pushVideoBiz;

    private List<BizHandler> listByUserIdOpenVideoBiz;

    private List<BizHandler> getHotVideoBiz;

    private List<BizHandler> listByUserIdVideoBiz;

    private List<BizHandler> publishVideoBiz;

    private List<BizHandler> followFeedBiz;

    @PostConstruct
    public void init() {
        this.listVideoByFavoritesBiz = bizBuilder.getBizHandler("listVideoByFavoritesBiz");
        this.searchVideoBiz = bizBuilder.getBizHandler("searchVideoBiz");
        this.banVideoBiz = bizBuilder.getBizHandler("banVideoBiz");
        this.startVideoBiz = bizBuilder.getBizHandler("startVideoBiz");
        this.favoriteVideoBiz = bizBuilder.getBizHandler("favoriteVideoBiz");
        this.getVideoByIdBiz = bizBuilder.getBizHandler("getVideoByIdBiz");
        this.getVideoByTypeBiz = bizBuilder.getBizHandler("getVideoByTypeBiz");
        this.shareVideoBiz = bizBuilder.getBizHandler("shareVideoBiz");
        this.auditVideoBiz = bizBuilder.getBizHandler("auditVideoBiz");
        this.getVideoByTypeBiz = bizBuilder.getBizHandler("getVideoByTypeBiz");
        this.deleteVideoByIdBiz = bizBuilder.getBizHandler("deleteVideoByIdBiz");
        this.addHistoryVideoBiz = bizBuilder.getBizHandler("addHistoryVideoBiz");
        this.getHistoryVideoBiz = bizBuilder.getBizHandler("getHistoryVideoBiz");
        this.pushSimilarVideoBiz = bizBuilder.getBizHandler("pushSimilarVideoBiz");
        this.pushVideoBiz = bizBuilder.getBizHandler("pushVideoBiz");
        this.listByUserIdOpenVideoBiz = bizBuilder.getBizHandler("listByUserIdOpenVideoBiz");
        this.getHotVideoBiz = bizBuilder.getBizHandler("getHotVideoBiz");
        this.listByUserIdVideoBiz = bizBuilder.getBizHandler("listByUserIdVideoBiz");
        this.publishVideoBiz = bizBuilder.getBizHandler("publishVideoBiz");
        this.followFeedBiz = bizBuilder.getBizHandler("followFeedBiz");
    }

    public void followFeed(BizContext context) {
        execute(followFeedBiz, context);
    }

    public void publishVideo(BizContext context) {
        execute(publishVideoBiz, context);
    }

    public void listByUserIdVideo(BizContext context) {
        execute(listByUserIdVideoBiz, context);
    }

    public void getHotVideo(BizContext context) {
        execute(getHotVideoBiz, context);
    }

    public void listByUserIdOpenVideo(BizContext context) {
        execute(listByUserIdOpenVideoBiz, context);
    }

    public void pushVideo(BizContext bizContext) {
        execute(pushVideoBiz, bizContext);
    }

    public void pushSimilarVideo(BizContext bizContext) {
        execute(pushSimilarVideoBiz, bizContext);
    }

    public void getHistoryVideo(BizContext bizContext) {
        execute(getHistoryVideoBiz, bizContext);
    }

    public void addHistoryVideo(BizContext bizContext) {
        execute(addHistoryVideoBiz, bizContext);
    }

    public void listVideoByFavorites(BizContext bizContext) {
        execute(listVideoByFavoritesBiz, bizContext);
    }

    public void banVideo(BizContext bizContext) {
        execute(banVideoBiz, bizContext);
    }

    public void getVideoById(BizContext bizContext) {
        execute(getVideoByIdBiz, bizContext);
    }

    public void deleteVideoById(BizContext bizContext) {
        execute(deleteVideoByIdBiz, bizContext);
    }

    public void auditVideo(BizContext bizContext) {
        execute(auditVideoBiz, bizContext);
    }

    public void startVideo(BizContext bizContext) {
        execute(startVideoBiz, bizContext);
    }

    public void favoriteVideo(BizContext bizContext) {
        execute(favoriteVideoBiz, bizContext);
    }

    public void getVideoByType(BizContext bizContext) {
        execute(getVideoByTypeBiz, bizContext);
    }

    public void shareVideo(BizContext bizContext) {
        execute(shareVideoBiz, bizContext);
    }

    public void searchVideo(BizContext bizContext) {
        execute(searchVideoBiz, bizContext);
    }
}
