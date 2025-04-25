package com.veeo.video.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veeo.common.config.QiNiuConfig;
import com.veeo.common.entity.user.Favorites;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.entity.vo.Model;
import com.veeo.common.entity.vo.UpdateUserVO;
import com.veeo.common.entity.vo.UserModel;
import com.veeo.common.holder.UserHolder;
import com.veeo.common.util.R;
import com.veeo.user.api.UserClient;
import com.veeo.video.service.FavoritesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.Collections;
import java.util.HashSet;
import java.util.List;


@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/veeo/customer")
public class CustomerController {


    final QiNiuConfig qiNiuConfig;

    private final UserClient userClient;

    private final FavoritesService favoritesService;

    public CustomerController(QiNiuConfig qiNiuConfig, UserClient userClient, FavoritesService favoritesService) {
        this.qiNiuConfig = qiNiuConfig;
        this.userClient = userClient;
        this.favoritesService = favoritesService;
    }


    /**
     * 获取个人信息
     */
    @GetMapping("/getInfo/{userId}")
    public R getInfo(@PathVariable Long userId){
        log.info("getInfo userId:{}", userId);
        return R.ok().data(userClient.getInfo(userId));
    }

    /**
     * 获取用户信息
     */
    @GetMapping(value = {"/getInfo", "/getInfo/"})
    public R getDefaultInfo(){
        log.info("getInfo UserHolder.get() {}", UserHolder.get());
        return R.ok().data(userClient.getInfo(UserHolder.get()));
    }

    /**
     * 获取关注人员
     */
    @GetMapping("/follows")
    public R getFollows(BasePage basePage, Long userId){
        return R.ok().data(userClient.getFollows(userId, basePage));
    }

    /**
     * 获取粉丝
     */
    @GetMapping("/fans")
    public R getFans(BasePage basePage,Long userId){
        return R.ok().data(userClient.getFans(userId,basePage));
    }


    /**
     * 获取所有的收藏夹
     */
    @GetMapping("/favorites")
    public R listFavorites(){
        final Long userId = UserHolder.get();
        List<Favorites> favorites = favoritesService.listByUserId(userId);
        return R.ok().data(favorites);
    }


    /**
     * 获取指定收藏夹
     */
    @GetMapping("/favorites/{id}")
    public R getFavorites(@PathVariable Long id){
        return R.ok().data(favoritesService.getById(id));
    }

    /**
     * 添加/修改收藏夹
     */
    @PostMapping("/favorites")
    public R saveOrUpdateFavorites(@RequestBody @Validated Favorites favorites){
        Long userId = UserHolder.get();
        Long id = favorites.getId();
        favorites.setUserId(userId);
        int count = (int) favoritesService.count(new LambdaQueryWrapper<Favorites>()
                .eq(Favorites::getName, favorites.getName()).eq(Favorites::getUserId, userId).ne(Favorites::getId,favorites.getId()));
        if (count == 1){
            return R.error().message("已存在相同名称的收藏夹");
        }
        favoritesService.saveOrUpdate(favorites);
        return R.ok().message(id !=null ? "修改成功" : "添加成功");
    }

    /**
     * 删除收藏夹
     */
    @DeleteMapping("/favorites/{id}")
    public R deleteFavorites(@PathVariable Long id){
        favoritesService.remove(id,UserHolder.get());
        return R.ok().message("删除成功");
    }


    /**
     * 订阅分类
     */
    @PostMapping("/subscribe")
    public R subscribe(@RequestParam(required = false) String types){
        final HashSet<Long> typeSet = new HashSet<>();
        String msg = "取消订阅";
        if (!ObjectUtils.isEmpty(types)){
            for (String s : types.split(",")) {
                typeSet.add(Long.parseLong(s));
            }
            msg = "订阅成功";
        }
        userClient.subscribe(typeSet);
        return R.ok().message(msg);
    }

    /**
     * 获取用户订阅的分类
     */
    @GetMapping("/subscribe")
    public R listSubscribeType(){
        return R.ok().data(userClient.listSubscribeType(UserHolder.get()));
    }


    /**
     * 获取用户没订阅的分类
     */
    @GetMapping("/noSubscribe")
    public R listNoSubscribeType(){
        return R.ok().data(userClient.listNoSubscribeType(UserHolder.get()));
    }

    /**
     * 关注/取关
     */
    @PostMapping("/follows")
    public R follows(@RequestParam Long followsUserId){

        return R.ok().message(userClient.follows(followsUserId) ? "已关注" : "已取关");
    }

    /**
     * 用户停留时长修改模型
     */
    @PostMapping("/updateUserModel")
    public R updateUserModel(@RequestBody Model model){
        final Double score = model.getScore();
        if (score == -0.5 || score == 1.0){
            final UserModel userModel = new UserModel();
            userModel.setUserId(UserHolder.get());
            userModel.setModels(Collections.singletonList(model));
            userClient.updateUserModel(userModel);
        }
        return R.ok();
    }

    /**
     * 获取用户上传头像的token
     */
    @GetMapping("/avatar/token")
    public R avatarToken(){
        return R.ok().data(qiNiuConfig.imageUploadToken());
    }

    /**
     *  修改用户信息
     */
    @PutMapping
    public R updateUser(@RequestBody @Validated UpdateUserVO user){
        userClient.updateUser(user);
        return R.ok().message("修改成功");
    }


}
