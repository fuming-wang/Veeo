package com.veeo.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.authority.AuthorityUtils;
import com.veeo.common.entity.user.*;
import com.veeo.user.mapper.PermissionMapper;
import com.veeo.user.service.PermissionService;
import com.veeo.user.service.RolePermissionService;
import com.veeo.user.service.UserRoleService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service

public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Resource
    private UserRoleService userRoleService;

    @Resource
    private RolePermissionService rolePermissionService;

    @Resource
    private AuthorityUtils authorityUtils;


    @Override
    public Map<String, Object> initMenu(Long uId) {

        // 创建返回结果map
        Map<String, Object> data = new HashMap<>();
        List<Menu> menus = new ArrayList<>();
        List<Menu> parentMenu = new ArrayList<>();

        // 封装权限集合
        Set<String> set = new HashSet<>();

        // 根据当期用户获取菜单
        // 根据用户id查询对应的角色id
        List<Long> rIds = userRoleService.list(new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, uId)
                        .select(UserRole::getRoleId))
                .stream().map(UserRole::getRoleId).collect(Collectors.toList());

        if (ObjectUtils.isEmpty(rIds)) {
            return Collections.emptyMap();
        }
        // 根据角色查询对应的权限id
        List<Integer> pIds = rolePermissionService.list(new LambdaQueryWrapper<RolePermission>()
                        .in(RolePermission::getRoleId, rIds)
                        .select(RolePermission::getPermissionId))
                .stream().map(RolePermission::getPermissionId)
                .collect(Collectors.toList());

        // 根据权限id查出权限
        // 查出所有权限-->转成对应的菜单对象
        list(new LambdaQueryWrapper<Permission>()
                .in(Permission::getId, pIds))
                .stream()
                .sorted(new PermissionComparator())
                .forEach(permission -> {
                    Menu menu = new Menu();
                    BeanUtils.copyProperties(permission, menu);
                    menu.setTitle(permission.getName());
                    menus.add(menu);
                });
        // list转树形结构
        // 1. 先找到根节点
        for (Menu menu : menus) {
            // 校验是根节点以及根节点不为按钮的节点
            if (menu.getPId().compareTo(0L) == 0 && menu.getIsMenu() != 1) {
                menu.setChild(new ArrayList<>());
                parentMenu.add(menu);
            }
        }

        // 根据根节点找到子节点
        parentMenu.forEach(menu -> {
            menu.getChild().add(findChild(menu, menus, set));
        });

        // 保存用户权限
        authorityUtils.setAuthority(uId, set);

        MenuKey menuKey1 = new MenuKey();
        MenuKey menuKey2 = new MenuKey();
        menuKey1.setTitle("首页");
        menuKey1.setHref("page/welcome.html?t=1");
        menuKey2.setTitle("Veeo");
        menuKey2.setHref("/index.html");
        data.put("menuInfo", parentMenu);
        data.put("homeInfo", menuKey1);
        data.put("logoInfo", menuKey2);
        return data;
    }

    @Override
    public List<Permission> treeSelect() {

        // 创建返回结果
        List<Permission> data = new ArrayList<>();

        // 查出所有权限 非按钮
        List<Permission> permissions = list(new LambdaQueryWrapper<Permission>()
                .ne(Permission::getIsMenu, 1));

        // 找到根节点
        for (Permission permission : permissions) {
            if (permission.getPId().compareTo(0L) == 0) {
                permission.setChildren(new ArrayList<>());
                data.add(permission);
            }
        }

        // 根据根节点找到子节点
        for (Permission datum : data) {
            datum.getChildren().add(findTreeSelectChildren(datum, permissions));
        }

        return data;
    }

    @Override
    @Transactional
    public void removeMenu(Long id) {
        // 封装id集合
        List<Long> ids = new ArrayList<>();
        findPermissionId(id, ids);
        ids.add(id);
        removeByIds(ids);
        rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>()
                .in(RolePermission::getPermissionId, ids));

    }

    private void findPermissionId(Long id, List<Long> ids) {

        list(new LambdaQueryWrapper<Permission>().eq(Permission::getPId, id)
                .select(Permission::getId))
                .forEach(permission -> {
                    ids.add(permission.getId());
                    findPermissionId(permission.getId(), ids);
                });
    }

    private Permission findTreeSelectChildren(Permission datum, List<Permission> permissions) {

        datum.setChildren(new ArrayList<>());
        for (Permission permission : permissions) {
            if (datum.getId().compareTo(permission.getPId()) == 0) {
                datum.getChildren().add(findTreeSelectChildren(permission, permissions));
            }
        }
        return datum;
    }

    private Menu findChild(Menu menu, List<Menu> menus, Set<String> set) {

        menu.setChild(new ArrayList<>());
        for (Menu m : menus) {
            if (!ObjectUtils.isEmpty(m.getPath())) {
                set.add(m.getPath());
            }
            if (m.getIsMenu() != 1) {
                if (menu.getId().compareTo(m.getPId()) == 0) {
                    // 递归调用该方法
                    menu.getChild().add(findChild(m, menus, set));
                }
            }
        }
        return menu;
    }

    // Permission比较器
    private static class PermissionComparator implements Comparator<Permission> {
        @Override
        public int compare(Permission o1, Permission o2) {
            return -o1.getSort() - o2.getSort();
        }
    }
}
