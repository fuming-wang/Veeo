package com.veeo.user.controller;

import com.veeo.common.entity.Captcha;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.vo.FindPWVO;
import com.veeo.common.entity.vo.RegisterVO;
import com.veeo.common.util.JwtUtils;
import com.veeo.common.util.R;
import com.veeo.user.service.LoginService;
import com.veeo.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;


@Slf4j
@RestController
@RequestMapping("/veeo/login")
public class LoginController {

    private final LoginService loginService;
    private final UserService userService;

    public LoginController(LoginService loginService, UserService userService) {
        this.loginService = loginService;
        this.userService = userService;
    }


    @RequestMapping("/test/{key}")
    public R test(@PathVariable String key){
        System.out.println("执行了测试代码");
        key += 11111;
        return R.ok().data(key);
    }

    @RequestMapping("/test2")
    public R test2(@RequestBody @Validated User user){

        System.out.println("user:" + user);
        return R.ok().data(user);
    }


    /**
     * 登录
     */
    @PostMapping
    public R login(@RequestBody @Validated User user){
        user = loginService.login(user);
        System.out.println(user);
        // 登录成功，生成token
        String token = JwtUtils.getJwtToken(user.getId(), user.getNickName());
        HashMap<Object, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("name", user.getNickName());
        map.put("user", user);
        return R.ok().data(map);
    }

    /**
     * 获取图形验证码
     */
    @GetMapping("/captcha.jpg/{uuId}")
    public void captcha(HttpServletResponse response, @PathVariable String uuId) throws IOException {
        System.out.println(uuId);
        loginService.captcha(uuId, response);
    }


    /**
     * 获取验证码
     */
    @PostMapping("/getCode")
    public R getCode(@RequestBody @Validated Captcha captcha) throws Exception {
        if (!loginService.getCode(captcha)) {
            return R.error().message("验证码错误");
        }
        return R.ok().message("发送成功,请耐心等待");
    }


    /**
     * 检测邮箱验证码
     */
    @PostMapping("/check")
    public R check(String email, Integer code){
        loginService.checkCode(email,code);
        return R.ok().message("验证成功");
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public R register(@RequestBody @Validated RegisterVO registerVO) throws Exception {
        if (!loginService.register(registerVO)) {
            return R.error().message("注册失败,验证码错误");
        }
        return R.ok().message("注册成功");
    }

    /**
     * 找回密码
     */
    @PostMapping("/findPassword")
    public R findPassword(@RequestBody @Validated FindPWVO findPWVO, HttpServletResponse response){
        final Boolean b = loginService.findPassword(findPWVO);
        return R.ok().message(b ? "修改成功" : "修改失败,验证码不正确");
    }

}
