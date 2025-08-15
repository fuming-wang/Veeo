package com.veeo.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.veeo.common.entity.Captcha;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.vo.FindPWVO;
import com.veeo.common.entity.vo.RegisterVO;
import com.veeo.common.exception.BaseException;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.user.constant.RedisConstant;
import com.veeo.user.service.CaptchaService;
import com.veeo.user.service.LoginService;
import com.veeo.user.service.UserService;
import com.veeo.user.utils.SafePassword;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;


@Slf4j
@Service
public class LoginServiceImpl implements LoginService {


    private final UserService userService;

    private final CaptchaService captchaService;

    private final RedisCacheUtil redisCacheUtil;

    public LoginServiceImpl(UserService userService, CaptchaService captchaService,
                            RedisCacheUtil redisCacheUtil) {
        this.userService = userService;
        this.captchaService = captchaService;
        this.redisCacheUtil = redisCacheUtil;
    }

    /**
     * 登录
     */
    @Override
    public User login(User user) {
        String password = user.getPassword();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        user = userService.getOne(wrapper.eq(User::getEmail, user.getEmail()));
        if (ObjectUtils.isEmpty(user)) {
            throw new BaseException("没有该账号");
        }
        boolean result = SafePassword.verify(password, user.getPassword());
        if (!result) {
            throw new BaseException("密码不一致");
        }
        return user;
    }

    @Override
    public Boolean checkCode(String email, Integer code) {
        if (ObjectUtils.isEmpty(email) || ObjectUtils.isEmpty(code)) {
            throw new BaseException("参数为空");
        }
        Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + email);
        if (!code.toString().equals(o)) {
            throw new BaseException("验证码不正确");
        }
        return true;
    }

    @Override
    public void captcha(String uuId, HttpServletResponse response) throws IOException {
        if (ObjectUtils.isEmpty(uuId)) {
            throw new IllegalArgumentException("uuid不能为空");
        }
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        BufferedImage image = captchaService.getCaptcha(uuId);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }

    @Override
    public Boolean getCode(Captcha captcha) throws Exception {
        return captchaService.validate(captcha);
    }

    @Override
    public Boolean register(RegisterVO registerVO) throws Exception {
        // 注册成功后删除图形验证码
        if (userService.register(registerVO)) {
            String id = registerVO.getUuid();
            redisCacheUtil.del(RedisConstant.CAPTCHA_CACHE + id);
            return true;
        }
        return false;
    }

    @Override
    public Boolean findPassword(FindPWVO findPWVO) {
        return userService.findPassword(findPWVO);
    }
}
