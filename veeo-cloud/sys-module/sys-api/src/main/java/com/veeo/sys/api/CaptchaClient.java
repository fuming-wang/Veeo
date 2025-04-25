package com.veeo.sys.api;

import com.veeo.common.entity.Captcha;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.awt.image.BufferedImage;


@FeignClient(value = "sys-service", contextId = "captcha-client", path = "/api/captcha")
public interface CaptchaClient {


    @RequestMapping("/getCaptcha")
    BufferedImage getCaptcha(@RequestParam String uuId);

    @RequestMapping("/validate")
    boolean validate(@RequestBody Captcha captcha) throws Exception;

    @RequestMapping("/removeById")
    boolean removeById(@RequestParam String id);
}
