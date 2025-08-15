package com.veeo.common.config;

import com.veeo.common.util.RedisCacheUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;



@Component
public class LocalCache {

    private static final String LOCAL_CACHE = "localCache:";

    @Resource
    private RedisCacheUtil redisCacheUtil;

    public void put(String key, Object val){
        redisCacheUtil.set(LOCAL_CACHE + key, val);
    }

    public Boolean containsKey(String key){
        if (key == null) {
            return false;
        }
        return redisCacheUtil.get(LOCAL_CACHE + key) != null;
    }

    public void rem(String key) {
        if (key == null) {
            return;
        }
        redisCacheUtil.del(LOCAL_CACHE + key);
    }
}
