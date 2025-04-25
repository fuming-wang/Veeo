package com.veeo.video.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.entity.user.FavoritesVideo;

import com.veeo.video.mapper.FavoritesVideoMapper;
import com.veeo.video.service.FavoritesVideoService;
import org.springframework.stereotype.Service;


@Service
public class FavoritesVideoServiceImpl extends ServiceImpl<FavoritesVideoMapper, FavoritesVideo> implements FavoritesVideoService {

}
