package com.veeo.video.limit;


import com.veeo.common.exception.LimiterException;
import com.veeo.common.holder.UserHolder;
import com.veeo.common.util.RedisCacheUtil;
import com.veeo.video.constant.RedisConstant;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.ObjectUtils;

@Aspect
public class LimiterAop {


    @Resource
    private RedisCacheUtil redisCacheUtil;


    /**
     * 拦截
     */
    @Around("@annotation(limiter)")
    public Object restriction(ProceedingJoinPoint joinPoint, Limit limiter) throws Throwable {
        Long userId = UserHolder.get();
        int limitCount = limiter.limit();
        String msg = limiter.msg();
        long time = limiter.time();
        // 缓存是否存在
        String key = RedisConstant.VIDEO_LIMIT + userId;
        Object o1 = redisCacheUtil.get(key);
        if (ObjectUtils.isEmpty(o1)) {
            redisCacheUtil.set(key, 1, time);
        } else {
            if (Integer.parseInt(o1.toString()) > limitCount) {
                throw new LimiterException(msg);
            }
            redisCacheUtil.incr(key, 1);
        }
        return joinPoint.proceed();
    }


}