package com.veeo.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.veeo.common.entity.user.Permission;

import java.util.List;
import java.util.Map;


public interface PermissionService extends IService<Permission> {

    Map<String, Object> initMenu(Long userId);

    List<Permission> treeSelect();

    void removeMenu(Long id);
}
