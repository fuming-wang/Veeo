package com.veeo.user.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.entity.vo.UpdateUserVO;
import com.veeo.common.entity.vo.UserModel;
import com.veeo.common.entity.vo.UserVO;
import com.veeo.common.holder.UserHolder;
import com.veeo.common.query.DataBase;
import com.veeo.common.query.QueryDTO;
import com.veeo.common.query.QueryWrapperUtil;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.user.client.UserClient;
import com.veeo.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserApi implements UserClient {


    @Resource
    private UserService userService;

    @Override
    public Result<User> getById(Long id) {
        return ResultUtil.getSucRet(userService.getById(id));
    }

    @Override
    public void addSearchHistory(Long userId, String search) {
        userService.addSearchHistory(userId, search);
    }

    @Override
    public Result<UserVO> getInfo(Long userId) {
        return ResultUtil.getSucRet(userService.getInfo(userId));
    }

    @Override
    public Result<List<User>> list(Collection<Long> userIds) {
        return ResultUtil.getSucRet(userService.list(userIds));
    }

    @Override
    public Result<List<User>> list(QueryDTO queryDTO) {
        LambdaQueryWrapper<User> queryWrapper = QueryWrapperUtil.convert(queryDTO.getConditions(), DataBase.USER);
        return ResultUtil.getSucRet(userService.list(queryWrapper));
    }

    @Override
    public Result<Collection<Type>> listSubscribeType(Long userId) {

        return ResultUtil.getSucRet(userService.listSubscribeType(userId));
    }

    @Override
    public Result<Collection<String>> searchHistory(Long userId) {

        return ResultUtil.getSucRet(userService.searchHistory(userId));
    }

    @Override
    public void deleteSearchHistory(Long userId) {

        userService.deleteSearchHistory(userId);
    }

    @Override
    public Result<Page<User>> getFollows(Long userId, BasePage basePage) {

        return ResultUtil.getSucRet(userService.getFollows(userId, basePage));
    }

    @Override
    public Result<Page<User>> getFans(Long userId, BasePage basePage) {

        return ResultUtil.getSucRet(userService.getFans(userId, basePage));
    }

    @Override
    public void subscribe(Long userId, Set<Long> typeIds) {
        UserHolder.set(userId);
        userService.subscribe(typeIds);
    }

    @Override
    public Result<Collection<Type>> listNoSubscribeType(Long aLong) {

        return ResultUtil.getSucRet(userService.listNoSubscribeType(aLong));
    }

    @Override
    public Result<Boolean> follows(Long userId, Long followsUserId) {
        UserHolder.set(userId);
        return ResultUtil.getSucRet(userService.follows(followsUserId));
    }

    @Override
    public void updateUserModel(UserModel userModel) {

        userService.updateUserModel(userModel);
    }

    @Override
    public void updateUser(Long userId, UpdateUserVO user) {
        UserHolder.set(userId);
        userService.updateUser(user);
    }


}
