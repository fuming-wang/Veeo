package com.veeo.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.entity.Setting;
import com.veeo.video.mapper.SettingMapper;
import com.veeo.video.service.SettingService;
import org.springframework.stereotype.Service;


/**
 * 服务实现类
 */
@Service
public class SettingServiceImpl extends ServiceImpl<SettingMapper, Setting> implements SettingService {

}
