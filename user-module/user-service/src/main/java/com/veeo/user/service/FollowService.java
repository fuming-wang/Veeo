package com.veeo.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.veeo.common.entity.user.Follow;
import com.veeo.common.entity.vo.BasePage;

import java.util.Collection;


public interface FollowService extends IService<Follow> {
    /**
     * 获取关注数量
     */
    int getFollowCount(Long userId);

    /**
     * 获取粉丝数量
     */
    int getFansCount(Long userId);

    /**
     * 获取关注人员且按照关注时间排序
     */
    Collection<Long> getFollow(Long userId, BasePage basePage);


    /**
     * 获取粉丝人员且安排关注时间排序
     */
    Collection<Long> getFans(Long userId, BasePage basePage);

    /**
     * 关注/取关
     */
    Boolean follows(Long followId, Long userId);

    /**
     * userId 是否关注 followId
     */
    Boolean isFollows(Long followId, Long userId);
}
