package com.veeo.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.code.kaptcha.Producer;
import com.veeo.common.constant.RedisConstant;
import com.veeo.common.entity.Captcha;
import com.veeo.common.exception.BaseException;
import com.veeo.common.util.DateUtil;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.sys.mapper.CaptchaMapper;
import com.veeo.sys.service.CaptchaService;
import com.veeo.sys.service.EmailService;
import org.springframework.stereotype.Service;


import java.awt.image.BufferedImage;
import java.util.Date;

/**
 * 系统验证码 服务实现类
 */
@Service
public class CaptchaServiceImpl extends ServiceImpl<CaptchaMapper, Captcha> implements CaptchaService {


    private final Producer producer;

    private final EmailService emailService;

    private final RedisCacheUtil redisCacheUtil;

    public CaptchaServiceImpl(Producer producer, EmailService emailService, RedisCacheUtil redisCacheUtil) {
        this.producer = producer;
        this.emailService = emailService;
        this.redisCacheUtil = redisCacheUtil;
    }

    @Override
    public BufferedImage getCaptcha(String uuId) {
        String code = this.producer.createText();
        Captcha captcha = new Captcha();
        captcha.setUuid(uuId);
        captcha.setCode(code);
        captcha.setExpireTime(DateUtil.addDateMinutes(new Date(),5));
        this.save(captcha);
        return producer.createImage(code);
    }

    @Override
    public boolean validate(Captcha captcha) throws Exception {
        String email = captcha.getEmail();
        final String code1 = captcha.getCode();
        captcha = this.getOne(new LambdaQueryWrapper<Captcha>().eq(Captcha::getUuid, captcha.getUuid()));
        if (captcha == null) throw new BaseException("uuId为空");

        this.remove(new LambdaQueryWrapper<Captcha>().eq(Captcha::getUuid, captcha.getUuid()));
        if(!captcha.getCode().equals(code1)){
            throw new BaseException("code错误");
        }
        if(captcha.getExpireTime().getTime()<=System.currentTimeMillis()){
            throw new BaseException("uuid过期");
        }
        if (!code1.equals(captcha.getCode())){
            return false;
        }

        String code = getSixCode();
        redisCacheUtil.set(RedisConstant.EMAIL_CODE + email, code, RedisConstant.EMAIL_CODE_TIME);
        emailService.send(email,"注册验证码:"+code+",验证码5分钟之内有效");
        return true;
    }


    public static String getSixCode(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int code = (int) (Math.random()*10);
            builder.append(code);
        }
        return builder.toString();
    }
}
