package com.veeo.sys.controller;

import com.veeo.common.entity.Captcha;
import com.veeo.sys.api.CaptchaClient;
import com.veeo.sys.service.CaptchaService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.io.Serializable;


@Slf4j
@RestController
@RequestMapping("/api/captcha")
public class APICaptchaController implements CaptchaClient {


    private final CaptchaService captchaService;

    public APICaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @Override
    public BufferedImage getCaptcha(@RequestParam String uuId) {
        if (uuId == null) {
            log.error("uuId is null");
        }
        return captchaService.getCaptcha(uuId);
    }

    @Override
    public boolean validate(@RequestBody Captcha captcha) throws Exception {
        if (captcha == null) {
            log.error("captcha is null");
        }
        return captchaService.validate(captcha);
    }

    @Override
    public boolean removeById(@RequestParam String id) {
        if(id == null) {
            log.error("id is null");
        }
        return captchaService.removeById(id);
    }
}
