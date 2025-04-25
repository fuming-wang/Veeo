package com.veeo.common.authority;


import com.veeo.common.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class BaseAuthority implements AuthorityVerify {

    private final AuthorityUtils authorityUtils;

    public BaseAuthority(AuthorityUtils authorityUtils) {
        this.authorityUtils = authorityUtils;
    }

    @Override
    public Boolean authorityVerify(HttpServletRequest request, String[] permissions) {
        String jwtToken = request.getHeader("token");
        if (!JwtUtils.checkToken(jwtToken)) {
            return false;
        }
        // 获取当前用户权限
        Long uId = JwtUtils.getUserId(jwtToken);
        for (String permission : permissions) {
            if (!authorityUtils.verify(uId, permission)) {
                return false;
            }
        }

        return true;
    }
}
