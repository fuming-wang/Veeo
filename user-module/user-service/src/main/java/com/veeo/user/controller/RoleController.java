package com.veeo.user.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.veeo.common.authority.Authority;
import com.veeo.common.entity.user.Role;
import com.veeo.common.entity.user.RolePermission;
import com.veeo.common.entity.user.Tree;
import com.veeo.common.entity.user.UserRole;
import com.veeo.common.entity.vo.AssignRoleVO;
import com.veeo.common.entity.vo.AuthorityVO;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.user.service.RolePermissionService;
import com.veeo.user.service.RoleService;
import com.veeo.user.service.UserRoleService;
import jakarta.annotation.Resource;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/authorize/role")
public class RoleController {

    @Resource
    private RoleService roleService;

    @Resource
    private RolePermissionService rolePermissionService;

    @Resource
    private UserRoleService userRoleService;


    @GetMapping("/treeList")
    @Authority("permission:treeList")
    public List<Tree> treeList() {
        return roleService.tree();
    }


    @PostMapping("/assignRole")
    @Authority("user:assignRole")
    public Result<Void> assignRole(@RequestBody AssignRoleVO assignRoleVO) {

        return roleService.gaveRole(assignRoleVO);
    }


    @GetMapping("/getUserRole/{userId}")
    @Authority("role:getRole")
    public List<Long> getRole(@PathVariable Integer userId) {
        return userRoleService.list(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId).select(UserRole::getRoleId))
                .stream().map(UserRole::getRoleId).collect(Collectors.toList());
    }

    /**
     * 初始化角色
     */
    @GetMapping("/initRole")
    @Authority("role:initRole")
    public List<Map<String, Object>> initRole() {
        // 查出所有角色

        return roleService.list().stream()
                .map(role -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("value", role.getId());
                    data.put("title", role.getName());
                    return data;
                }).collect(Collectors.toList());
    }


    @GetMapping("/list")
    @Authority("role:list")
    public Result<List<Role>> list(BasePage basePage, @RequestParam(required = false) String name) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!ObjectUtils.isEmpty(name), Role::getName, name);
        IPage<Role> iPage = basePage.rolePage();
        IPage<Role> page = roleService.page(iPage, wrapper);
        return ResultUtil.getSucRet(page.getRecords()).count(page.getRecords().size());
    }

    /**
     * 添加角色
     */
    @PostMapping
    @Authority("role:add")
    public Result<Void> add(@RequestBody Role role) {
        roleService.save(role);
        return ResultUtil.getSucRet();
    }

    /**
     * 修改角色
     */
    @PutMapping
    @Authority("role:update")
    public Result<Void> update(@RequestBody Role role) {
        roleService.updateById(role);
        return ResultUtil.getSucRet();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @Authority("role:delete")
    public Result<Void> delete(@PathVariable String id) {
        return roleService.removeRole(id);
    }


    /**
     * 给角色分配权限
     * 给角色分配权限前先把该角色的权限都删了
     */
    @PostMapping("/authority")
    @Authority("role:authority")
    public Result<Void> authority(@RequestBody AuthorityVO authorityVO) {
        return roleService.gavePermission(authorityVO);
    }

    /**
     * 获取角色权限
     */
    @GetMapping("/getPermission/{id}")
    @Authority("role:getPermission")
    public Integer[] getPermission(@PathVariable Integer id) {
        return rolePermissionService.list(
                        new LambdaQueryWrapper<RolePermission>()
                                .eq(RolePermission::getRoleId, id)
                                .select(RolePermission::getPermissionId))
                .stream().map(RolePermission::getPermissionId).toArray(Integer[]::new);
    }

}

