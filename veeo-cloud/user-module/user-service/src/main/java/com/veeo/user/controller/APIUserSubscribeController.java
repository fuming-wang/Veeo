package com.veeo.user.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veeo.common.entity.user.UserSubscribe;
import com.veeo.common.query.DataBase;
import com.veeo.common.query.QueryDTO;
import com.veeo.common.query.QueryWrapperUtil;
import com.veeo.user.api.UserSubscribeClient;
import com.veeo.user.service.UserSubscribeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/userSubscribe")
public class APIUserSubscribeController implements UserSubscribeClient {

    private final UserSubscribeService userSubscribeService;

    public APIUserSubscribeController(UserSubscribeService userSubscribeService) {
        this.userSubscribeService = userSubscribeService;
    }


    @Override
    public boolean remove(QueryDTO queryDTO) {
        if(queryDTO == null){
            log.error("queryDTO is null");
        }
        LambdaQueryWrapper<UserSubscribe> queryWrapper = QueryWrapperUtil.convert(queryDTO.getConditions(), DataBase.USER_SUBSCRIBE);
        return userSubscribeService.remove(queryWrapper);
    }

    @Override
    public boolean saveBatch(Collection<UserSubscribe> entityList) {
        if(entityList == null){
            log.error("entityList is null");
        }
        return saveBatch(entityList);
    }

    @Override
    public List<UserSubscribe> list(QueryDTO queryDTO) {
        if(queryDTO == null){
            log.error("queryDTO is null");
        }
        LambdaQueryWrapper<UserSubscribe> queryWrapper = QueryWrapperUtil.convert(queryDTO.getConditions(), DataBase.USER_SUBSCRIBE);
        return userSubscribeService.list(queryWrapper);
    }
}
