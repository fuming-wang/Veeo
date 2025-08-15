package com.veeo.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.veeo.common.entity.user.Follow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


public interface FollowMapper extends BaseMapper<Follow> {

    @Select("select * from follow where user_id = #{userId} and follow_id = #{follow_id}")
    Follow select(@Param("followId") Long followId, @Param("userId") Long userId);

    @Update("update follow set is_delete = 0 where user_id = #{userId} and follow_id = #{follow_id}")
    int updateFollow(@Param("userId") Long userId, @Param("followId") Long followId);
}
