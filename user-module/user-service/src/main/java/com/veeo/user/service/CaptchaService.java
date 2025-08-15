package com.veeo.user.service;

import com.veeo.common.entity.Captcha;

import java.awt.image.BufferedImage;


public interface CaptchaService {

    BufferedImage getCaptcha(String uuId);

    boolean validate(Captcha captcha) throws Exception;
}
