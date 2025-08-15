package com.veeo.video.config;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.veeo.video.handler.BizHandler;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName BuilderBiz
 * @Description
 * @Author wangfuming
 * @Date 2025/7/24 21:26
 * @Version 1.0.0
 */

@Component
public class BizBuilder implements InitializingBean {

    private final Map<String, List<BizHandler>> bizHandlerMap = new ConcurrentHashMap<>();

    @Resource
    private BizChainProperties bizChainProperties;

    @Resource
    private ApplicationContext applicationContext;

    public List<BizHandler> getBizHandler(String bizType) {
        return bizHandlerMap.get(bizType);
    }

    @Override
    public void afterPropertiesSet() {
        bizChainProperties.getConfigs().forEach((k, v) -> {
            List<BizHandler> bizHandlers = Lists.newArrayList();
            for (String beanName : v.getChain()) {
                BizHandler handler = applicationContext.getBean(beanName, BizHandler.class);
                bizHandlers.add(handler);
            }
            bizHandlerMap.put(k, bizHandlers);
        });
    }
}
