package com.veeo.common.authority;


import com.veeo.common.exception.AuthorityException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 权限校验
 * 多个aop的情况下使用 @Order(1) 来指定顺序
 */
@Aspect
@Component
public class AuthorityAop {

    private final HttpServletRequest request;

    private final AuthorityUtils authorityUtils;

    public AuthorityAop(HttpServletRequest request, AuthorityUtils authorityUtils) {
        this.request = request;
        this.authorityUtils = authorityUtils;
    }

    /**
     * 自定义校验aop
     */
    @Around("@annotation(authority)")
    public Object authority(ProceedingJoinPoint joinPoint, Authority authority) throws Throwable {

        Boolean result;

        if (!authorityUtils.getPostAuthority()) {
            // 全局校验类
            AuthorityVerify verifyObject = authorityUtils.getGlobalVerify();
            result = verifyObject.authorityVerify(request, authority.value());
            if (!result) throw new AuthorityException("权限不足");
        }
        return joinPoint.proceed();
    }

}
