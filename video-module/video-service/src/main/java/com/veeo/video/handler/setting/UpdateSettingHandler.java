package com.veeo.video.handler.setting;

import com.veeo.common.config.LocalCache;
import com.veeo.common.entity.Setting;
import com.veeo.video.config.BizContext;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.SettingService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @ClassName UpdateSettingHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/23 22:26
 * @Version 1.0.0
 */
@Service
public class UpdateSettingHandler implements BizHandler {

    @Resource
    private SettingService settingService;

    @Resource
    private LocalCache localCache;

    @Override
    public void handle(BizContext context) {

        Setting setting = context.getSetting();
        settingService.updateById(setting);
        for (String s : setting.getAllowIp().split(",")) {
            localCache.put(s, true);
        }
        context.setResult(true);
    }
}
