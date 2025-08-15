package com.veeo.video.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veeo.common.entity.user.Favorites;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.vo.*;
import com.veeo.common.holder.UserHolder;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.user.client.UserClient;
import com.veeo.video.biz.FavoriteBiz;
import com.veeo.video.service.FavoritesService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/veeo/customer")
public class CustomerController {

    @Resource
    private FavoriteBiz favoriteBiz;

    @Resource
    private UserClient userClient;

    @Resource
    private FavoritesService favoritesService;



    /**
     * 获取个人信息
     */
    @GetMapping("/getInfo/{userId}")
    public Result<UserVO> getInfo(@PathVariable Long userId) {
        return userClient.getInfo(userId);
    }

    /**
     * 获取用户信息
     */
    @GetMapping(value = {"/getInfo", "/getInfo/"})
    public Result<UserVO> getDefaultInfo() {
        return userClient.getInfo(UserHolder.get());
    }

    /**
     * 获取关注人员
     */
    @GetMapping("/follows")
    public Result<Page<User>> getFollows(BasePage basePage, Long userId) {
        return userClient.getFollows(userId, basePage);
    }

    /**
     * 获取粉丝
     */
    @GetMapping("/fans")
    public Result<Page<User>> getFans(BasePage basePage, Long userId) {
        return userClient.getFans(userId, basePage);
    }


    /**
     * 获取所有的收藏夹
     */
    @GetMapping("/favorites")
    public Result<List<Favorites>> listFavorites() {
        Long userId = UserHolder.get();
        List<Favorites> favorites = favoritesService.listByUserId(userId);
        return ResultUtil.getSucRet(favorites);
    }


    /**
     * 获取指定收藏夹
     */
    @GetMapping("/favorites/{id}")
    public Result<Favorites> getFavorites(@PathVariable Long id) {
        return ResultUtil.getSucRet(favoritesService.getById(id));
    }

    /**
     * 添加/修改收藏夹
     */
    @PostMapping("/favorites")
    public Result<Void> saveOrUpdateFavorites(@RequestBody @Validated Favorites favorites) {
        Long userId = UserHolder.get();
        Long id = favorites.getId();
        favorites.setUserId(userId);
        int count = (int) favoritesService.count(new LambdaQueryWrapper<Favorites>()
                .eq(Favorites::getName, favorites.getName()).eq(Favorites::getUserId, userId).ne(Favorites::getId, favorites.getId()));
        if (count == 1) {
            return ResultUtil.getFailRet("已存在相同名称的收藏夹");
        }
        favoritesService.saveOrUpdate(favorites);
        return ResultUtil.getSucRet(id != null ? "修改成功" : "添加成功");
    }

    /**
     * 删除收藏夹
     */
    @DeleteMapping("/favorites/{id}")
    public Result<Void> deleteFavorites(@PathVariable Long id) {
        Result<Boolean> deleteResult = favoriteBiz.deleteFavorites(id, UserHolder.get());
        if (deleteResult.isFailed()) {
            return ResultUtil.getFailRet("删除失败" + deleteResult.getMessage());
        }
        return ResultUtil.getSucRet("删除成功");
    }


    /**
     * 订阅分类
     */
    @PostMapping("/subscribe")
    public Result<Void> subscribe(@RequestParam(required = false) String types) {
        HashSet<Long> typeSet = new HashSet<>();
        String msg = "取消订阅";
        if (!ObjectUtils.isEmpty(types)) {
            for (String s : types.split(",")) {
                typeSet.add(Long.parseLong(s));
            }
            msg = "订阅成功";
        }
        userClient.subscribe(UserHolder.get(), typeSet);
        return ResultUtil.getSucRet(msg);
    }

    /**
     * 获取用户订阅的分类
     */
    @GetMapping("/subscribe")
    public Result<Collection<Type>> listSubscribeType() {
        return userClient.listSubscribeType(UserHolder.get());
    }


    /**
     * 获取用户没订阅的分类
     */
    @GetMapping("/noSubscribe")
    public Result<Collection<Type>> listNoSubscribeType() {
        return userClient.listNoSubscribeType(UserHolder.get());
    }

    /**
     * 关注/取关
     */
    @PostMapping("/follows")
    public Result<Void> follows(@RequestParam Long followsUserId) {

        return ResultUtil.getSucRet(userClient.follows(UserHolder.get(), followsUserId).getData() ? "已关注" : "已取关");
    }

    /**
     * 用户停留时长修改模型
     */
    @PostMapping("/updateUserModel")
    public Result<Void> updateUserModel(@RequestBody Model model) {
        Double score = model.getScore();
        if (score == -0.5 || score == 1.0) {
            List<String> labels = Arrays.asList(model.getLabels().split(","));
            UserModel userModel = UserModel.buildUserModel(labels, model.getId(), score);
            userModel.setUserId(UserHolder.get());
            userClient.updateUserModel(userModel);
        }
        return ResultUtil.getSucRet();
    }

    /**
     * 获取用户上传头像的token
     */
    @GetMapping("/avatar/token")
    public Result<String> avatarToken() {
        // todo
        return ResultUtil.getFailRet("未实现");
    }

    /**
     * 修改用户信息
     */
    @PutMapping
    public Result<Void> updateUser(@RequestBody @Validated UpdateUserVO user) {
        userClient.updateUser(UserHolder.get(), user);
        return ResultUtil.getSucRet("修改成功");
    }
}
