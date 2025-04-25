package com.veeo.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeo.common.constant.VeeoHttpConstant;
import com.veeo.common.entity.user.User;
import com.veeo.common.holder.UserHolder;
import com.veeo.common.util.R;
import com.veeo.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Component
public class AdminInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserService userService;

    public AdminInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
       if(request.getRequestURI().equals("/error")){
           log.error("请求/error");
           return false;
       }
        Long id = Long.valueOf(request.getHeader(VeeoHttpConstant.USER_ID));
        User user = userService.getById(id);
        if (ObjectUtils.isEmpty(user)){
            response(R.error().message("用户不存在"),response);
            return false;
        }
        UserHolder.set(id);
        return true;


    }

    private boolean response(R r, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().println(objectMapper.writeValueAsString(r));
        response.getWriter().flush();
        return false;
    }
}
