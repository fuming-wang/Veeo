package com.veeo.user.controller;


import com.veeo.common.authority.Authority;
import com.veeo.common.entity.user.Permission;
import com.veeo.common.holder.UserHolder;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.user.service.PermissionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/authorize/permission")
public class PermissionController {

    @Resource
    private PermissionService permissionService;

    /**
     * 权限列表
     **/
    @GetMapping("/list")
    @Authority("permission:list")
    public List<Permission> list() {
        return permissionService.list();
    }


    /**
     * 新增权限时树形结构
     */
    @GetMapping("/treeSelect")
    @Authority("permission:treeSelect")
    public List<Permission> treeSelect() {

        return permissionService.treeSelect();
    }

    /**
     * 添加权限
     */
    @PostMapping
    @Authority("permission:add")
    public Result<Void> add(@RequestBody Permission permission) {
        permission.setIcon("fa " + permission.getIcon());
        permissionService.save(permission);

        return ResultUtil.getSucRet();
    }

    /**
     * 修改权限
     */
    @PutMapping
    @Authority("permission:update")
    public Result<Void> update(@RequestBody Permission permission) {
        permission.setIcon("fa " + permission.getIcon());
        permissionService.updateById(permission);
        return ResultUtil.getSucRet();
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @Authority("permission:delete")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.removeMenu(id);
        return ResultUtil.getSucRet("删除成功");
    }

    /**
     * 初始化菜单
     */
    @GetMapping("/initMenu")
    public Map<String, Object> initMenu() {
        log.info("initMenu");
        return permissionService.initMenu(UserHolder.get());
    }
}

