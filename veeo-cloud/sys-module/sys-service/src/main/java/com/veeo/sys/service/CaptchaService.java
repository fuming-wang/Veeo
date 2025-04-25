package com.veeo.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.veeo.common.entity.Captcha;


import java.awt.image.BufferedImage;


public interface CaptchaService extends IService<Captcha> {


    BufferedImage getCaptcha(String uuId);

    boolean validate(Captcha captcha) throws Exception;
}
