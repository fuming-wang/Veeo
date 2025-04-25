package com.veeo.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.entity.vo.UpdateUserVO;
import com.veeo.common.entity.vo.UserModel;
import com.veeo.common.entity.vo.UserVO;
import com.veeo.common.query.DataBase;
import com.veeo.common.query.QueryDTO;
import com.veeo.common.query.QueryWrapperUtil;
import com.veeo.user.api.UserClient;
import com.veeo.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class APIUserController implements UserClient {

    private final UserService userService;

    public APIUserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User getById(Long id) {
        if (id == null) {
            log.error("id is null");
        }
        User user = userService.getById(id);
        return user;
    }

    @Override
    public void addSearchHistory(Long userId, String search) {
        if (userId == null || search == null) {
            log.error("userId and search is null");
        }
        userService.addSearchHistory(userId, search);
    }

    @Override
    public UserVO getInfo(Long userId) {
        if (userId == null) {
            log.error("userId is null");
        }
        return userService.getInfo(userId);
    }

    @Override
    public List<User> list(Collection<Long> userIds) {
        if (userIds == null) {
            log.error("userIds is null");
        }
        return userService.list(userIds);
    }

    @Override
    public List<User> list(QueryDTO queryDTO) {
        if (queryDTO == null) {
            log.error("queryDTO is null");
        }
        LambdaQueryWrapper<User> queryWrapper = QueryWrapperUtil.convert(queryDTO.getConditions(), DataBase.USER);
        List<User> list = userService.list(queryWrapper);
        return list;
    }



    @Override
    public Collection<Type> listSubscribeType(Long userId) {
        if (userId == null) {
            log.error("userId is null");
        }
        return userService.listSubscribeType(userId);
    }

    @Override
    public Collection<String> searchHistory(Long userId) {
        if (userId == null) {
            log.error("userId is null");
        }
        return userService.searchHistory(userId);
    }

    @Override
    public void deleteSearchHistory(Long userId) {
        if (userId == null) {
            log.error("userId is null");
        }
        userService.deleteSearchHistory(userId);
    }

    @Override
    public Page<User> getFollows(Long userId, BasePage basePage) {
        if (userId == null || basePage == null) {
            log.error("userId and basePage is null");
        }
        return userService.getFollows(userId, basePage);
    }

    @Override
    public Page<User> getFans(Long userId, BasePage basePage) {
        if (userId == null || basePage == null) {
            log.error("userId and basePage is null");
        }
        return userService.getFans(userId, basePage);
    }

    @Override
    public void subscribe(Set<Long> typeIds) {
        if (typeIds == null) {
            log.error("typeIds is null");
        }
        userService.subscribe(typeIds);
    }

    @Override
    public Collection<Type> listNoSubscribeType(Long aLong) {
        if (aLong == null) {
            log.error("aLong is null");
        }
        return userService.listNoSubscribeType(aLong);
    }

    @Override
    public boolean follows(Long followsUserId) {
        if (followsUserId == null) {
            log.error("followsUserId is null");
        }
        return userService.follows(followsUserId);
    }

    @Override
    public void updateUserModel(UserModel userModel) {
        if (userModel == null) {
            log.error("userModel is null");
        }
        userService.updateUserModel(userModel);
    }

    @Override
    public void updateUser(UpdateUserVO user) {
        if (user == null) {
            log.error("user is null");
        }
        userService.updateUser(user);
    }


}
