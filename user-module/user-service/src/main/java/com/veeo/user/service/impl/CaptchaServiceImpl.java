package com.veeo.user.service.impl;


import com.google.code.kaptcha.Producer;
import com.veeo.common.entity.Captcha;
import com.veeo.common.exception.BaseException;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.user.constant.RedisConstant;
import com.veeo.user.service.CaptchaService;
import com.veeo.user.service.EmailService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;


/**
 * 系统验证码 服务实现类
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Resource
    private Producer producer;

    @Resource
    private EmailService emailService;

    @Resource
    private RedisCacheUtil redisCacheUtil;


    private static String getSixCode() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int code = (int) (Math.random() * 10);
            builder.append(code);
        }
        return builder.toString();
    }

    @Override
    public BufferedImage getCaptcha(String uuId) {
        // 调用第三方服务生成code
        String code = producer.createText();
        // 将uuid作为key, code作为value存储redis中，过期时5分钟
        redisCacheUtil.set(RedisConstant.CAPTCHA_CACHE + uuId, code, 300);
        return producer.createImage(code);
    }

    @Override
    public boolean validate(Captcha captcha) {
        // 邮箱
        String email = captcha.getEmail();
        // 输入验证码
        String code = captcha.getCode();
        // 实际验证码
        String redisCode = redisCacheUtil.get(RedisConstant.CAPTCHA_CACHE + captcha.getUuid()).toString();
        // uuId为空 或者 uuId过期
        if (redisCode == null) {
            throw new BaseException("验证码过期了");
        }
        // 验证码错误
        if (!redisCode.equals(code)) {
            throw new BaseException("验证码错误");
        }
        // 生成验证码，随机6位数字
        String emailCode = getSixCode();
        // 放入redis中
        redisCacheUtil.set(RedisConstant.EMAIL_CODE + email, emailCode, RedisConstant.EMAIL_CODE_TIME);
        // 发送
        emailService.send(email, "注册验证码:" + code + ", 验证码5分钟之内有效");
        return true;
    }
}
