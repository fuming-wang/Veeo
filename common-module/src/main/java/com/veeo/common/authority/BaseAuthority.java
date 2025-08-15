package com.veeo.common.authority;


import com.veeo.common.constant.VeeoHttpConstant;
import com.veeo.common.util.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class BaseAuthority implements AuthorityVerify {


    @Resource
    private AuthorityUtils authorityUtils;

    @Override
    public Boolean authorityVerify(HttpServletRequest request, String[] permissions) {
        String jwtToken = request.getHeader(VeeoHttpConstant.USER_LOGIN_TOKEN);
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
