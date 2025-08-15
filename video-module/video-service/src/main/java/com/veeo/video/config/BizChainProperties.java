package com.veeo.video.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @ClassName BizChainProperties
 * @Description
 * @Author wangfuming
 * @Date 2025/7/24 21:36
 * @Version 1.0.0
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "biz")
@Component
public class BizChainProperties {

    private Map<String, ChainConfig> configs;

    @Setter
    @Getter
    public static class ChainConfig {
        private List<String> chain;
    }

}
