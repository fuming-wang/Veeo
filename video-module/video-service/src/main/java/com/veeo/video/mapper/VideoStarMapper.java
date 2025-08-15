package com.veeo.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.veeo.common.entity.video.VideoStar;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


public interface VideoStarMapper extends BaseMapper<VideoStar> {

    @Select("SELECT * FROM video_star WHERE video_id = #{videoId} AND user_id = #{userId} LIMIT 1")
    VideoStar selectAllStatesByUserAndVideo(@Param("videoId") Long videoId, @Param("userId") Long userId);

    @Update("UPDATE video_star SET is_deleted = #{is_deleted}, gmt_updated = NOW() WHERE id = #{id}")
    void updateExistStarStatusById(@Param("id") Long id, @Param("is_deleted") Integer is_deleted);
}
