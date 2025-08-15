package com.veeo.user.service;


import com.veeo.common.entity.Captcha;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.vo.FindPWVO;
import com.veeo.common.entity.vo.RegisterVO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


public interface LoginService {

    /**
     * 登录
     */
    User login(User user);

    /**
     * 检查验证码
     */
    Boolean checkCode(String email, Integer code);

    /**
     * 生成图形验证码
     */
    void captcha(String uuid, HttpServletResponse response) throws IOException;

    /**
     * 获取验证码
     */
    Boolean getCode(Captcha captcha) throws Exception;

    /**
     * 注册账号
     */
    Boolean register(RegisterVO registerVO) throws Exception;

    /**
     * 找回密码
     */
    Boolean findPassword(FindPWVO findPWVO);
}
