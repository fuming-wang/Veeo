package com.veeo.user.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veeo.common.entity.user.UserSubscribe;
import com.veeo.common.query.DataBase;
import com.veeo.common.query.QueryDTO;
import com.veeo.common.query.QueryWrapperUtil;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.user.client.UserSubscribeClient;
import com.veeo.user.service.UserSubscribeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/userSubscribe")
public class UserSubscribeApi implements UserSubscribeClient {

    @Resource
    private UserSubscribeService userSubscribeService;

    @Override
    public Result<Boolean> remove(QueryDTO queryDTO) {

        LambdaQueryWrapper<UserSubscribe> queryWrapper = QueryWrapperUtil.convert(queryDTO.getConditions(), DataBase.USER_SUBSCRIBE);
        return ResultUtil.getSucRet(userSubscribeService.remove(queryWrapper));

    }

    @Override
    public Result<Boolean> saveBatch(Collection<UserSubscribe> entityList) {
        return ResultUtil.getSucRet(userSubscribeService.saveBatch(entityList));
    }

    @Override
    public Result<List<UserSubscribe>> list(QueryDTO queryDTO) {

        LambdaQueryWrapper<UserSubscribe> queryWrapper = QueryWrapperUtil.convert(queryDTO.getConditions(), DataBase.USER_SUBSCRIBE);
        return ResultUtil.getSucRet(userSubscribeService.list(queryWrapper));
    }
}
