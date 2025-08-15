package com.veeo.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.entity.user.RolePermission;
import com.veeo.user.mapper.RolePermissionMapper;
import com.veeo.user.service.RolePermissionService;
import org.springframework.stereotype.Service;


@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

}
