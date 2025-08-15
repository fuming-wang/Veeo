package com.veeo.video.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veeo.common.constant.VeeoHttpConstant;
import com.veeo.common.holder.UserHolder;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Component
public class AdminInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) throws IOException {
        if ("/error".equals(request.getRequestURI())) {
            return false;
        }
        long id;
        try {
            id = Long.parseLong(request.getHeader(VeeoHttpConstant.USER_ID));
        } catch (NumberFormatException e) {
            return response(ResultUtil.getFailRet("验证登录失败, 请重试"), response);
        }
        UserHolder.set(id);
        return true;
    }

    private boolean response(Result<?> result, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().println(objectMapper.writeValueAsString(result));
        response.getWriter().flush();
        return false;
    }
}
