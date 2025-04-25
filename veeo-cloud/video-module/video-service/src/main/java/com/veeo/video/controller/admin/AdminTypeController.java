package com.veeo.video.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.veeo.common.authority.Authority;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.util.R;
import com.veeo.video.service.TypeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



@RestController
@CrossOrigin
@RequestMapping("/admin/type")
public class AdminTypeController {

    private final TypeService typeService;

    public AdminTypeController(TypeService typeService) {
        this.typeService = typeService;
    }


    @GetMapping("/{id}")
    @Authority("admin:type:get")
    public R get(@PathVariable Long id){
        return R.ok().data(typeService.getById(id));
    }


    @GetMapping("/page")
    @Authority("admin:type:page")
    public R page(BasePage basePage){
        final IPage page = typeService.page(basePage.page(), null);
        return R.ok().data(page.getRecords()).count(page.getTotal());
    }

    @PostMapping
    @Authority("admin:type:add")
    public R add(@RequestBody @Validated Type type){
        long count = typeService.count(new LambdaQueryWrapper<Type>().eq(Type::getName, type.getName()).ne(Type::getId,type.getId()));
        if (count == 1) return R.error().message("分类已存在");
        typeService.save(type);
        return R.ok().message("添加成功");
    }

    @PutMapping
    @Authority("admin:type:update")
    public R update(@RequestBody @Validated Type type){
        long count =  typeService.count(new LambdaQueryWrapper<Type>().eq(Type::getName, type.getName()).ne(Type::getId,type.getId()));
        if (count == 1) return R.error().message("分类已存在");
        typeService.updateById(type);
        return R.ok().message("修改成功");
    }

    @DeleteMapping("/{id}")
    @Authority("admin:type:delete")
    public R delete(@PathVariable Long id){
        typeService.removeById(id);
        return R.ok().message("删除成功");
    }

}
