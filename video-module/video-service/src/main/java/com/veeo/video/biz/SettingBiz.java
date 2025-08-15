package com.veeo.video.biz;

import com.veeo.video.config.BizBuilder;
import com.veeo.video.config.BizContext;
import com.veeo.video.handler.BizHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName SettingBiz
 * @Description
 * @Author wangfuming
 * @Date 2025/7/23 22:15
 * @Version 1.0.0
 */
@Service
public class SettingBiz extends BizExecute {

    @Resource
    private BizBuilder bizBuilder;

    private List<BizHandler> getSettingBiz;
    private List<BizHandler> updateSettingBiz;
    private List<BizHandler> startProcessSettingBiz;

    @PostConstruct
    public void init() {
        this.getSettingBiz = bizBuilder.getBizHandler("getSettingBiz");
        this.updateSettingBiz = bizBuilder.getBizHandler("updateSettingBiz");
        this.startProcessSettingBiz = bizBuilder.getBizHandler("startProcessSettingBiz");
    }

    public void getSetting(BizContext bizContext) {
        execute(this.getSettingBiz, bizContext);
    }

    public void updateSetting(BizContext bizContext) {
        execute(this.updateSettingBiz, bizContext);
    }

    public void startProcess(BizContext bizContext) {
        execute(this.startProcessSettingBiz, bizContext);
    }

}
