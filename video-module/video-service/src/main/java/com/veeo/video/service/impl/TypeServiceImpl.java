package com.veeo.video.service.impl;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.entity.video.Type;
import com.veeo.video.mapper.TypeMapper;
import com.veeo.video.service.TypeService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@Service
public class TypeServiceImpl extends ServiceImpl<TypeMapper, Type> implements TypeService {


    @Override
    public List<String> getLabels(Long typeId) {
        return this.getById(typeId).buildLabel();
    }

    @Override
    public List<String> random10Labels() {
        List<Type> types = list();
        Collections.shuffle(types);
        List<String> labels = Lists.newArrayList();
        types.stream()
                .flatMap(type -> type.buildLabel().stream())
                .limit(10)
                .forEach(labels::add);
        return labels;
    }
}
