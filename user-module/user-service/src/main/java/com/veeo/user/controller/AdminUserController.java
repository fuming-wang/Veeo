package com.veeo.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.veeo.common.authority.Authority;
import com.veeo.common.entity.user.Role;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.user.UserRole;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.user.service.RoleService;
import com.veeo.user.service.UserRoleService;
import com.veeo.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/admin/user")
public class AdminUserController {


    @Resource
    private UserService userService;

    @Resource
    private UserRoleService userRoleService;

    @Resource
    private RoleService roleService;


    @GetMapping("/list")
    @Authority("admin:user:list")
    public Result<List<User>> list() {
        return ResultUtil.getSucRet(userService.list(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/page")
    @Authority("admin:user:page")
    public Result<List<User>> list(BasePage basePage, @RequestParam(required = false) String name) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(!ObjectUtils.isEmpty(name), User::getEmail, name);
        IPage<User> page = userService.page(basePage.userPage(), wrapper);
        // 查出用户角色中间表
        Map<Long, List<UserRole>> userRoleMap = userRoleService.list()
                .stream().collect(Collectors.groupingBy(UserRole::getUserId));
        // 根据角色查出角色表信息
        Map<Long, String> roleMap = roleService.list().stream().collect(Collectors.toMap(Role::getId, Role::getName));
        Map<Long, Set<String>> map = new HashMap<>();
        userRoleMap.forEach((uId, rIds) -> {
            Set<String> roles = new HashSet<>();
            for (UserRole rId : rIds) {
                roles.add(roleMap.get(rId.getRoleId()));
            }
            map.put(uId, roles);
        });
        for (User user : page.getRecords()) {
            user.setRoleName(map.get(user.getId()));
        }
        return ResultUtil.getSucRet(page.getRecords()).count(page.getTotal());
    }
}
