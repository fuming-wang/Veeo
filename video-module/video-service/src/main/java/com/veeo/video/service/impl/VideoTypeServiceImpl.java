package com.veeo.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.entity.video.VideoType;
import com.veeo.video.mapper.VideoTypeMapper;
import com.veeo.video.service.VideoTypeService;
import org.springframework.stereotype.Service;


@Service
public class VideoTypeServiceImpl extends ServiceImpl<VideoTypeMapper, VideoType> implements VideoTypeService {

}
