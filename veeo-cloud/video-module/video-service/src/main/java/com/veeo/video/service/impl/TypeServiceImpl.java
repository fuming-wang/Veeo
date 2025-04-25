package com.veeo.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.entity.video.Type;
import com.veeo.video.mapper.TypeMapper;
import com.veeo.video.service.TypeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
public class TypeServiceImpl extends ServiceImpl<TypeMapper, Type> implements TypeService {


    @Override
    public List<String> getLabels(Long typeId) {
        List<String> labels = this.getById(typeId).buildLabel();
        return labels;
    }

    @Override
    public List<String> random10Labels() {
        final List<Type> types = list();
        Collections.shuffle(types);
        final ArrayList<String> labels = new ArrayList<>();
        for (Type type : types) {
            for (String label : type.buildLabel()) {
                if (labels.size() == 10){
                    return labels;
                }
                labels.add(label);
            }
        }
        return labels;
    }
}
