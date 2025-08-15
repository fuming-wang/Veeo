package com.veeo.common.authority;

import com.veeo.common.util.RedisCacheUtil;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * 在单体项目中可以利用局部缓存
 * 在微服务中，将组件设置为bean，同时将信息存储在redis中
 * key=user:permission:id, value = Set(.....)
 */
@Component
@Data
public class AuthorityUtils {

    private final static String USER_PERMISSION = "user:permission:";

    @Resource
    private RedisCacheUtil redisCacheUtil;
    /**
     * 过滤权限集合
     */
    private Set<String> filterPermission = new HashSet<>();
    /**
     * 全局校验示例，我们交给ioc来管理，不需要类进行实例化了
     */
    private AuthorityVerify verifyInstance;
    /**
     * 是否开启全局校验 默认为false不开启
     */
    private Boolean globalVerify = false;
    /**
     * 是否开启 @PostMapping 全局校验 默认为false不开启 获取 postAuthority 状态
     */
    private Boolean postAuthority = false;

    /**
     * 获取全局权限校验示例
     **/
    public AuthorityVerify getGlobalVerify() {
        return verifyInstance;
    }

    /**
     * 开启全局校验
     * 是不是可以直接使用bean注入呢?
     */
    public void setGlobalVerify(Boolean state, Object o) {
        if (o == null) {
            throw new NullPointerException();
        } else if (!(o instanceof AuthorityVerify)) {
            throw new ClassCastException(o.getClass() + " 类型不是 AuthorityVerify 实现类");
        }
        this.verifyInstance = (AuthorityVerify) o;
        globalVerify = state;
    }

    /**
     * 添加权限
     *
     * @param uId       用户id
     * @param authority 权限集合
     */
    public void setAuthority(Long uId, Collection<String> authority) {
        redisCacheUtil.set(USER_PERMISSION + uId, authority);
    }

    /**
     * 校验权限
     */
    @SuppressWarnings("unchecked")
    public Boolean verify(Long uId, String authority) {
        if (isEmpty(uId)) {
            return false;
        }
        Collection<String> permissions = (Collection<String>) redisCacheUtil.get(USER_PERMISSION + uId);
        return permissions.contains(authority);
    }

    /**
     * 排除权限
     */
    public void exclude(String... permissions) {
        filterPermission.addAll(Arrays.asList(permissions));
    }

    /**
     * 是否有过滤权限
     */
    public Boolean filterPermission(String permission) {
        return filterPermission.contains(permission);
    }

    /**
     * 判空
     */
    public Boolean isEmpty(Long uId) {
        return ObjectUtils.isEmpty(redisCacheUtil.get(USER_PERMISSION + uId));
    }
}
