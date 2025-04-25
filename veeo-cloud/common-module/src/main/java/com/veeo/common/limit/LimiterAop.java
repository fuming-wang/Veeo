package com.veeo.common.limit;

import com.veeo.common.constant.RedisConstant;
import com.veeo.common.exception.LimiterException;
import com.veeo.common.holder.UserHolder;
import com.veeo.common.util.RedisCacheUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;


@Aspect
public class LimiterAop {


    @Autowired
    private RedisCacheUtil redisCacheUtil;

    /**
     * 拦截
     * @param joinPoint
     * @param limiter
     * @return
     * @throws Throwable
     */
    @Before("@annotation(limiter)")
    public Object restriction(ProceedingJoinPoint joinPoint, Limit limiter) throws Throwable {
        Long userId = UserHolder.get();
        int limitCount = limiter.limit();
        String msg = limiter.msg();
        long time = limiter.time();
        // 缓存是否存在
        String key = RedisConstant.VIDEO_LIMIT + userId;
        Object o1 = redisCacheUtil.get(key);
        if (ObjectUtils.isEmpty(o1)){
            redisCacheUtil.set(key,1,time);
        }else {
            if (Integer.parseInt(o1.toString()) > limitCount){
                throw new LimiterException(msg);
            }
            redisCacheUtil.incr(key,1);
        }
        return joinPoint.proceed();
    }


}