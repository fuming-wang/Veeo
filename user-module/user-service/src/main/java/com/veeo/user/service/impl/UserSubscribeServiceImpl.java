package com.veeo.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.entity.user.UserSubscribe;
import com.veeo.user.mapper.UserSubscribeMapper;
import com.veeo.user.service.UserSubscribeService;
import org.springframework.stereotype.Service;


@Service
public class UserSubscribeServiceImpl extends ServiceImpl<UserSubscribeMapper, UserSubscribe> implements UserSubscribeService {
}
