package com.veeo.sys.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veeo.common.entity.Setting;
import com.veeo.common.query.DataBase;
import com.veeo.common.query.QueryDTO;
import com.veeo.common.query.QueryWrapperUtil;
import com.veeo.sys.api.SettingClient;
import com.veeo.sys.service.SettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.Serializable;
import java.util.List;



@Slf4j
@RestController
@RequestMapping("/api/setting")
public class APISettingController implements SettingClient {

    private final SettingService settingService;

    public APISettingController(SettingService settingService) {
        this.settingService = settingService;
    }


    @Override
    public Setting getById(Long id) {
        if (id == null) {
            log.error("id is null");
        }
        return settingService.getById(id);
    }

    @Override
    public List<Setting> list(QueryDTO queryDTO) {
        if (queryDTO == null) {
            log.error("queryDTO is null");
        }
        LambdaQueryWrapper<Setting> queryWrapper = QueryWrapperUtil.convert(queryDTO.getConditions(), DataBase.SETTING);

        return settingService.list(queryWrapper);
    }


    @Override
    public List<Setting> list() {
        log.info("list函数执行");
        return settingService.list();
    }

}
