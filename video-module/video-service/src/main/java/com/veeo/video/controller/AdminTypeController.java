package com.veeo.video.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.veeo.common.authority.Authority;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.video.service.TypeService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin/type")
public class AdminTypeController {

    @Resource
    private TypeService typeService;


    @GetMapping("/{id}")
    @Authority("admin:type:get")
    public Result<Type> get(@PathVariable Long id) {
        return ResultUtil.getSucRet(typeService.getById(id));
    }


    @GetMapping("/page")
    @Authority("admin:type:page")
    public Result<List<Type>> page(BasePage basePage) {
        IPage<Type> page = typeService.page(basePage.typePage(), null);
        return ResultUtil.getSucRet(page.getRecords()).count(page.getTotal());
    }

    @PostMapping
    @Authority("admin:type:add")
    public Result<Void> add(@RequestBody @Validated Type type) {
        long count = typeService.count(new LambdaQueryWrapper<Type>().eq(Type::getName, type.getName()).ne(Type::getId, type.getId()));
        if (count == 1) {
            return ResultUtil.getFailRet("分类已存在");
        }
        typeService.save(type);
        return ResultUtil.getSucRet("添加成功");
    }

    @PutMapping
    @Authority("admin:type:update")
    public Result<Void> update(@RequestBody @Validated Type type) {
        long count = typeService.count(new LambdaQueryWrapper<Type>().eq(Type::getName, type.getName()).ne(Type::getId, type.getId()));
        if (count == 1) {
            return ResultUtil.getFailRet("分类已存在");
        }
        typeService.updateById(type);
        return ResultUtil.getSucRet("修改成功");
    }

    @DeleteMapping("/{id}")
    @Authority("admin:type:delete")
    public Result<Void> delete(@PathVariable Long id) {
        typeService.removeById(id);
        return ResultUtil.getSucRet("删除成功");
    }

}
