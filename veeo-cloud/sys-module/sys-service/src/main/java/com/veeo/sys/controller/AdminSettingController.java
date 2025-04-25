package com.veeo.sys.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.veeo.common.authority.Authority;
import com.veeo.common.config.LocalCache;
import com.veeo.common.entity.Setting;
import com.veeo.common.entity.json.SettingScoreJson;
import com.veeo.common.util.R;
import com.veeo.sys.service.SettingService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/setting")
public class AdminSettingController {

    private final SettingService settingService;

    private ObjectMapper objectMapper = new ObjectMapper();

    public AdminSettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping
    @Authority("admin:setting:get")
    public R get() throws JsonProcessingException {
        Setting setting = settingService.list().get(0);
        SettingScoreJson settingScoreJson = objectMapper.readValue(setting.getAuditPolicy(), SettingScoreJson.class);
        setting.setSettingScoreJson(settingScoreJson);
        return R.ok().data(setting);
    }


    @PutMapping
    @Authority("admin:setting:update")
    public R update(@RequestBody @Validated Setting setting){
        settingService.updateById(setting);
        for (String s : setting.getAllowIp().split(",")) {
            LocalCache.put(s,true);
        }
        return R.ok().message("修改成功");
    }
}
