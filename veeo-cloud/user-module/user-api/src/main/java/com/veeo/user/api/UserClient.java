package com.veeo.user.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.vo.BasePage;
import com.veeo.common.entity.vo.UpdateUserVO;
import com.veeo.common.entity.vo.UserModel;
import com.veeo.common.entity.vo.UserVO;
import com.veeo.common.query.QueryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Set;


@FeignClient(value = "user-service", contextId = "user-client",path = "/api/user")
public interface UserClient {

    @RequestMapping("/getById")
    User getById(@RequestParam Long id);

    @RequestMapping("/addSearchHistory")
    void addSearchHistory(@RequestParam("userId") Long userId, @RequestParam("search") String search);

    @RequestMapping("/getInfo")
    UserVO getInfo(@RequestParam Long userId);

    @RequestMapping("/list")
    List<User> list(@RequestParam Collection<Long> userIds);

    @RequestMapping("/list2")
    List<User> list(@RequestBody QueryDTO queryDTO);

    @RequestMapping("/listSubscribeType")
    Collection<Type> listSubscribeType(@RequestParam Long userId);

    @RequestMapping("/searchHistory")
    Collection<String> searchHistory(@RequestParam Long userId);

    @RequestMapping("/deleteSearchHistory")
    void deleteSearchHistory(@RequestParam Long userId);

    @RequestMapping("/getFollows")
    Page<User> getFollows(@RequestParam Long userId, @RequestBody BasePage basePage);

    @RequestMapping("/getFans")
    Page<User> getFans(@RequestParam Long userId, @RequestBody BasePage basePage);

    @RequestMapping("/subscribe")
    void subscribe(@RequestParam Set<Long> typeIds);

    @RequestMapping("/listNoSubscribeType")
    Collection<Type> listNoSubscribeType(@RequestParam Long aLong);

    @RequestMapping("/follows")
    boolean follows(@RequestParam Long followsUserId);

    @RequestMapping("/updateUserModel")
    void updateUserModel(@RequestBody UserModel userModel);

    @RequestMapping("/updateUser")
    void updateUser(@RequestBody UpdateUserVO user);
}
