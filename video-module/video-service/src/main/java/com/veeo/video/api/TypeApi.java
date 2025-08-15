package com.veeo.video.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veeo.common.entity.video.Type;
import com.veeo.common.query.DataBase;
import com.veeo.common.query.QueryDTO;
import com.veeo.common.query.QueryWrapperUtil;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.video.client.TypeClient;
import com.veeo.video.service.TypeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/type")
public class TypeApi implements TypeClient {

    @Resource
    private TypeService typeService;

    @Override
    public Result<List<String>> getLabels(Long typeId) {

        return ResultUtil.getSucRet(typeService.getLabels(typeId));
    }

    @Override
    public Result<List<String>> random10Labels() {

        return ResultUtil.getSucRet(typeService.random10Labels());
    }

    @Override
    public Result<Collection<Type>> listByIds(Collection<Long> ids) {

        return ResultUtil.getSucRet(typeService.listByIds(ids));
    }

    @Override
    public Result<List<Type>> list(QueryDTO queryDTO) {

        LambdaQueryWrapper<Type> queryWrapper = QueryWrapperUtil.convert(queryDTO.getConditions(), DataBase.TYPE);

        return ResultUtil.getSucRet(typeService.list(queryWrapper));
    }
}
