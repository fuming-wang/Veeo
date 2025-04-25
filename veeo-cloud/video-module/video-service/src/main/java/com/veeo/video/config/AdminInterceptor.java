package com.veeo.video.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeo.common.constant.VeeoHttpConstant;
import com.veeo.common.entity.user.User;
import com.veeo.common.holder.UserHolder;
import com.veeo.common.util.R;
import com.veeo.user.api.UserClient;
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

    private final UserClient userClient;

    public AdminInterceptor(UserClient userClient) {
        this.userClient = userClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
       if(request.getRequestURI().equals("/error")){
           log.error("请求/error");
           return false;
       }
       // 我们假设所有的请求都来自网关转发，在网关进行了token校验
       // 获取userId 该请求可能来自网关转发，可能来自openfeign调用
       Long id = Long.valueOf(request.getHeader(VeeoHttpConstant.USER_ID));
       User user = userClient.getById(id);
       if (ObjectUtils.isEmpty(user)){
           response(R.error().message("用户不存在"), response);
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
