package com.veeo.user.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.veeo.common.entity.user.User;
import jakarta.annotation.PostConstruct;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.springframework.stereotype.Component;


@Component
public class MybatisPlusInitializer {

    @PostConstruct
    public void init() {
        // 手动加载 BaseEntity 的字段信息到缓存中
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), User.class);
    }
}
