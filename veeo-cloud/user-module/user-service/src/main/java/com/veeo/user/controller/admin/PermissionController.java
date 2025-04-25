package com.veeo.user.controller.admin;


import com.veeo.common.entity.user.Permission;
import com.veeo.common.holder.UserHolder;
import com.veeo.common.util.R;
import com.veeo.common.authority.Authority;
import com.veeo.user.service.PermissionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/authorize/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 权限列表
     * @return
     */
    @GetMapping("/list")
    @Authority("permission:list")
    public List<Permission> list(){
        return permissionService.list();
    }


    /**
     * 新增权限时树形结构
     * @return
     */
    @GetMapping("/treeSelect")
    @Authority("permission:treeSelect")
    public List<Permission> treeSelect(){
        List<Permission> data = permissionService.treeSelect();

        return data;
    }

    /**
     * 添加权限
     * @param permission
     * @return
     */
    @PostMapping
    @Authority("permission:add")
    public R add(@RequestBody Permission permission){
        permission.setIcon("fa " + permission.getIcon());
        permissionService.save(permission);

        return R.ok();
    }

    /**
     * 修改权限
     * @param permission
     * @return
     */
    @PutMapping
    @Authority("permission:update")
    public R update(@RequestBody Permission permission){
        permission.setIcon("fa " + permission.getIcon());
        permissionService.updateById(permission);
        return R.ok();

    }

    /**
     * 删除权限
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @Authority("permission:delete")
    public R delete(@PathVariable Long id){
        permissionService.removeMenu(id);
        return R.ok().message("删除成功");
    }


    /**
     * 初始化菜单
     * @return
     */
    @GetMapping("/initMenu")
    public Map<String, Object> initMenu(){
        log.info("initMenu");
        Map<String, Object> data = permissionService.initMenu(UserHolder.get());
        return data;
    }
}

