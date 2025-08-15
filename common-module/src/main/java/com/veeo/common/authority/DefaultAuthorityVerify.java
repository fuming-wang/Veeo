package com.veeo.common.authority;


import jakarta.servlet.http.HttpServletRequest;

/**
 * 权限校验具体实现类
 * todo 未实现，默认放行
 */
public class DefaultAuthorityVerify implements AuthorityVerify{

    @Override
    public Boolean authorityVerify(HttpServletRequest request, String[] permissions) {
        return true;
    }
}
