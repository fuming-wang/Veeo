package com.veeo.user.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.entity.vo.UpdateUserVO;
import com.veeo.common.entity.vo.UserModel;
import com.veeo.common.entity.vo.UserVO;
import com.veeo.common.query.QueryDTO;
import com.veeo.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Set;


@FeignClient(value = "user-service", contextId = "user-client",path = "/api/user")
public interface UserClient {

    @RequestMapping("/getById")
    Result<User> getById(@RequestParam Long id);

    @RequestMapping("/addSearchHistory")
    void addSearchHistory(@RequestParam Long userId, @RequestParam String search);

    @RequestMapping("/getInfo")
    Result<UserVO> getInfo(@RequestParam Long userId);

    @RequestMapping("/list")
    Result<List<User>> list(@RequestParam Collection<Long> userIds);

    @RequestMapping("/list2")
    Result<List<User>> list(@RequestBody QueryDTO queryDTO);

    @RequestMapping("/listSubscribeType")
    Result<Collection<Type>> listSubscribeType(@RequestParam Long userId);

    @RequestMapping("/searchHistory")
    Result<Collection<String>> searchHistory(@RequestParam Long userId);

    @RequestMapping("/deleteSearchHistory")
    void deleteSearchHistory(@RequestParam Long userId);

    @RequestMapping("/getFollows")
    Result<Page<User>> getFollows(@RequestParam Long userId, @RequestBody BasePage basePage);

    @RequestMapping("/getFans")
    Result<Page<User>> getFans(@RequestParam Long userId, @RequestBody BasePage basePage);

    @RequestMapping("/subscribe")
    void subscribe(@RequestParam Long userId, @RequestParam Set<Long> typeIds);

    @RequestMapping("/listNoSubscribeType")
    Result<Collection<Type>> listNoSubscribeType(@RequestParam Long aLong);

    @RequestMapping("/follows")
    Result<Boolean> follows(@RequestParam Long userId, @RequestParam Long followsUserId);

    @RequestMapping("/updateUserModel")
    void updateUserModel(@RequestBody UserModel userModel);

    @RequestMapping(value = "/updateUser", method = RequestMethod.POST)
    void updateUser(@RequestParam Long userId, @RequestBody UpdateUserVO user);
}
