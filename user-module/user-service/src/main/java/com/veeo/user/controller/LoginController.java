package com.veeo.user.controller;

import com.veeo.common.constant.VeeoHttpConstant;
import com.veeo.common.entity.Captcha;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.vo.FindPWVO;
import com.veeo.common.entity.vo.RegisterVO;
import com.veeo.common.util.JwtUtils;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.user.service.LoginService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/veeo/login")
public class LoginController {


    @Resource
    private LoginService loginService;


    /**
     * 登录
     */
    @PostMapping
    public Result<Map<Object, Object>> login(@RequestBody @Validated User user) {
        user = loginService.login(user);
        // 登录成功，生成token
        String token = JwtUtils.getJwtToken(user.getId(), user.getNickName());

        HashMap<Object, Object> map = new HashMap<>();
        map.put(VeeoHttpConstant.USER_LOGIN_TOKEN, token);
        map.put(VeeoHttpConstant.USER_NAME, user.getNickName());
        map.put(VeeoHttpConstant.USER, user);
        return ResultUtil.getSucRet(map);
    }

    /**
     * 获取图形验证码
     */
    @GetMapping("/captcha.jpg/{uuId}")
    public void captcha(HttpServletResponse response, @PathVariable String uuId) throws IOException {
        loginService.captcha(uuId, response);
    }


    /**
     * 获取验证码
     */
    @PostMapping("/getCode")
    public Result<String> getCode(@RequestBody @Validated Captcha captcha) throws Exception {
        if (!loginService.getCode(captcha)) {
            return ResultUtil.getFailRet("验证码错误");
        }
        return ResultUtil.getSucRet("发送成功,请耐心等待");
    }


    /**
     * 检测邮箱验证码
     */
    @PostMapping("/check")
    public Result<String> check(String email, Integer code) {
        loginService.checkCode(email, code);
        return ResultUtil.getSucRet("验证成功");
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody @Validated RegisterVO registerVO) throws Exception {
        if (!loginService.register(registerVO)) {
            return ResultUtil.getFailRet("注册失败,验证码错误");
        }
        return ResultUtil.getSucRet("注册成功");
    }

    /**
     * 找回密码
     */
    @PostMapping("/findPassword")
    public Result<String> findPassword(@RequestBody @Validated FindPWVO findPWVO, HttpServletResponse response) {
        Boolean b = loginService.findPassword(findPWVO);
        return ResultUtil.getSucRet(b ? "修改成功" : "修改失败,验证码不正确");
    }

}
