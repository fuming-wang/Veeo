package com.veeo.file.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.veeo.common.entity.File;
import com.veeo.file.mapper.FileMapper;
import com.veeo.file.service.FileService;
import org.springframework.stereotype.Service;

/**
 * 服务实现类
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {
}
