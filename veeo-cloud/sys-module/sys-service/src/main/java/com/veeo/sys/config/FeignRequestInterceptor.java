package com.veeo.sys.config;

import com.veeo.common.holder.UserHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        template.header("User-Id", String.valueOf(UserHolder.get()));
    }

}

