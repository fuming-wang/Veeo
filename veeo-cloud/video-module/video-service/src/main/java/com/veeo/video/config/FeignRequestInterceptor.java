package com.veeo.video.config;

import com.veeo.common.constant.VeeoHttpConstant;
import com.veeo.common.holder.UserHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {

        template.header(VeeoHttpConstant.USER_ID, String.valueOf(UserHolder.get()));
    }

}

