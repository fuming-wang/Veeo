package com.veeo.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.entity.user.Role;
import com.veeo.common.entity.user.RolePermission;
import com.veeo.common.entity.user.Tree;
import com.veeo.common.entity.user.UserRole;
import com.veeo.common.entity.vo.AssignRoleVO;
import com.veeo.common.entity.vo.AuthorityVO;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.user.mapper.RoleMapper;
import com.veeo.user.service.PermissionService;
import com.veeo.user.service.RolePermissionService;
import com.veeo.user.service.RoleService;
import com.veeo.user.service.UserRoleService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {


    @Resource
    private PermissionService permissionService;

    @Resource
    private RolePermissionService rolePermissionService;

    @Resource
    private UserRoleService userRoleService;


    @Override
    public List<Tree> tree() {

        List<Tree> trees = permissionService.list().stream().map(permission -> {
            Tree tree = new Tree();
            BeanUtils.copyProperties(permission, tree);
            tree.setTitle(permission.getName());
            tree.setSpread(true);
            return tree;
        }).collect(Collectors.toList());

        // 找到根节点
        List<Tree> parent = trees.stream()
                .filter(tree -> tree.getPId().compareTo(0L) == 0)
                .collect(Collectors.toList());

        parent.forEach(item -> {
            item.setChildren(new ArrayList<>());
            item.getChildren().add(findChildren(item, trees));
        });

        return parent;
    }


    @Override
    @Transactional
    public Result<Void> removeRole(String id) {
        try {
            // 删除角色权限中间表
            rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, id));
            // 删除角色表
            baseMapper.deleteById(id);
            // 删除用户角色表
            userRoleService.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultUtil.getFailRet("删除失败");
        }
        return ResultUtil.getSucRet("删除成功");
    }

    @Override
    @Transactional
    public Result<Void> gavePermission(AuthorityVO authorityVO) {
        try {
            rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, authorityVO.getRid()));
            List<RolePermission> list = new ArrayList<>();
            Integer rid = authorityVO.getRid();
            for (Integer pId : authorityVO.getPid()) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRoleId(rid);
                rolePermission.setPermissionId(pId);
                list.add(rolePermission);
            }
            rolePermissionService.saveBatch(list);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultUtil.getFailRet("分配权限失败");
        }
        return ResultUtil.getSucRet("分配权限成功");
    }

    @Override
    @Transactional
    public Result<Void> gaveRole(AssignRoleVO assignRoleVO) {
        // 获取被分配角色的用户信息
        Long uId = assignRoleVO.getUId();
        try {
            userRoleService.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, uId));
            List<UserRole> userRoles = new ArrayList<>();
            Arrays.stream(assignRoleVO.getRId()).forEach(id -> {
                UserRole userRole = new UserRole();
                userRole.setUserId(uId);
                userRole.setRoleId(id);
                userRoles.add(userRole);
            });
            userRoleService.saveBatch(userRoles);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResultUtil.getFailRet("分配角色失败");
        }
        return ResultUtil.getSucRet("分配角色成功");
    }

    private Tree findChildren(Tree datum, List<Tree> trees) {
        datum.setChildren(new ArrayList<>());
        trees.forEach(tree -> {
            if (tree.getPId().compareTo(datum.getId()) == 0) {
                datum.getChildren().add(findChildren(tree, trees));
            }
        });
        return datum;
    }
}
