package com.veeo.video.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.veeo.common.entity.video.Type;

import java.util.List;


public interface TypeService extends IService<Type> {

    /**
     * 获取分类下的标签
     */
    List<String> getLabels(Long typeId);

    /**
     * 随机获取标签
     */
    List<String> random10Labels();
}
