package com.veeo.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.veeo.common.entity.user.Role;
import com.veeo.common.entity.user.Tree;
import com.veeo.common.entity.vo.AssignRoleVO;
import com.veeo.common.entity.vo.AuthorityVO;
import com.veeo.common.util.Result;

import java.util.List;


public interface RoleService extends IService<Role> {

    List<Tree> tree();

    Result<Void> removeRole(String id);

    Result<Void> gavePermission(AuthorityVO authorityVO);

    Result<Void> gaveRole(AssignRoleVO assignRoleVO);

}
