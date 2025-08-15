package com.veeo.video.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.veeo.common.authority.Authority;
import com.veeo.common.entity.Setting;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.video.biz.SettingBiz;
import com.veeo.video.config.BizContext;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/setting")
public class AdminSettingController implements InitializingBean {

    @Resource
    private SettingBiz settingBiz;

    @GetMapping
    @Authority("admin:setting:get")
    public Result<Setting> get() throws JsonProcessingException {
        BizContext context = BizContext.create();
        settingBiz.getSetting(context);
        return ResultUtil.getSucRet(context.getSetting());
    }


    @PutMapping
    @Authority("admin:setting:update")
    public Result<Setting> update(@RequestBody @Validated Setting setting) {
        BizContext context = BizContext.create();
        context.setSetting(setting);
        settingBiz.updateSetting(context);
        if (context.getResult()) {
            return ResultUtil.getSucRet("修改成功");
        }
        return ResultUtil.getFailRet("修改失败");
    }

    @Override
    public void afterPropertiesSet() {
        BizContext context = BizContext.create();
        settingBiz.startProcess(context);
    }
}
