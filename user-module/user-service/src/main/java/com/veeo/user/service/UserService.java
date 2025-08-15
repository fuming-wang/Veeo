package com.veeo.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.vo.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;


public interface UserService extends IService<User> {

    /**
     * 注册
     */
    boolean register(RegisterVO registerVO) throws Exception;

    /**
     * 获取用户信息:
     * 1.用户基本信息
     * 2.关注数量
     * 3.粉丝数量
     */
    UserVO getInfo(Long userId);

    /**
     * 获取关注
     */
    Page<User> getFollows(Long userId, BasePage basePage);

    /**
     * 获取粉丝
     */
    Page<User> getFans(Long userId, BasePage basePage);

    /**
     * 获取用户基本信息
     */
    List<User> list(Collection<Long> userIds);

    /**
     * 订阅分类
     */
    void subscribe(Set<Long> typeIds);

    /**
     * 获取订阅分类
     */
    Collection<Type> listSubscribeType(Long userId);

    /**
     * 关注/取关
     */
    boolean follows(Long followsUserId);

    /**
     * 修改用户模型
     */
    void updateUserModel(UserModel userModel);

    /**
     * 找回密码
     */
    Boolean findPassword(FindPWVO findPWVO);

    /**
     * 修改用户资料
     */
    void updateUser(UpdateUserVO user);

    /**
     * 获取用户搜索记录
     *
     * @return 搜索的值
     */
    Collection<String> searchHistory(Long userId);

    /**
     * 添加搜索记录
     */
    void addSearchHistory(Long userId, String search);

    /**
     * 删除搜索记录
     */
    void deleteSearchHistory(Long userId);


    Collection<Type> listNoSubscribeType(Long aLong);

}
