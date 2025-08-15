package com.veeo.video.handler.setting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeo.common.entity.Setting;
import com.veeo.common.entity.json.SettingScoreJson;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.video.config.BizContext;
import com.veeo.video.constant.RedisConstant;
import com.veeo.video.handler.BizHandler;
import com.veeo.video.service.SettingService;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

/**
 * @ClassName SettingHandler
 * @Description
 * @Author wangfuming
 * @Date 2025/7/23 22:06
 * @Version 1.0.0
 */
@Service
public class GetSettingHandler implements BizHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private SettingService settingService;

    @Resource
    private RedisCacheUtil redisCacheUtil;

    @SneakyThrows
    @Override
    public void handle(BizContext context) {
        Setting setting;
        setting = (Setting) redisCacheUtil.get(RedisConstant.SETTING_CACHE);
        if (setting == null) {
            setting = settingService.list().getFirst();
        }
        SettingScoreJson settingScoreJson = objectMapper.readValue(setting.getAuditPolicy(), SettingScoreJson.class);
        setting.setSettingScoreJson(settingScoreJson);
        redisCacheUtil.set(RedisConstant.SETTING_CACHE, setting);
        context.setSetting(setting);
    }
}
