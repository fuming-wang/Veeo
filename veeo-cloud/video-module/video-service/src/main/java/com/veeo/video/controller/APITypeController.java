package com.veeo.video.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veeo.common.entity.video.Type;
import com.veeo.common.query.DataBase;
import com.veeo.common.query.QueryDTO;
import com.veeo.common.query.QueryWrapperUtil;
import com.veeo.video.api.TypeClient;
import com.veeo.video.service.TypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/type")
public class APITypeController implements TypeClient {

    private final TypeService typeService;

    public APITypeController(TypeService typeService) {
        this.typeService = typeService;
    }

    @Override
    public List<String> getLabels(Long typeId) {
        if (typeId == null) {
            log.error("typeId is null or empty");
        }
        return typeService.getLabels(typeId);
    }

    @Override
    public List<String> random10Labels() {

        return typeService.random10Labels();
    }

    @Override
    public Collection<Type> listByIds(Collection<Long> ids) {
        log.info("ids: {}", ids);
        if (ids == null || ids.isEmpty()) {
            log.error("ids is null or empty");
        }
        return typeService.listByIds(ids);
    }

    @Override
    public List<Type> list(QueryDTO queryDTO) {
        if (queryDTO == null) {
            log.error("queryDTO is null or empty");
        }
        LambdaQueryWrapper<Type> queryWrapper = QueryWrapperUtil.convert(queryDTO.getConditions(), DataBase.TYPE);
        return typeService.list(queryWrapper);
    }
}
