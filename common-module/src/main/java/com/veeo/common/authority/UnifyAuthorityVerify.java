package com.veeo.common.authority;

import com.veeo.common.constant.VeeoHttpConstant;
import com.veeo.common.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;


@Component(value = "postMappingAuthorityVerify")
public class UnifyAuthorityVerify extends DefaultAuthorityVerify {

    private final AuthorityUtils authorityUtils;

    public UnifyAuthorityVerify(AuthorityUtils authorityUtils) {
        this.authorityUtils = authorityUtils;
    }

    @Override
    public Boolean authorityVerify(HttpServletRequest request, String... permissions) {
        String jwtToken = request.getHeader(VeeoHttpConstant.USER_LOGIN_TOKEN);
        Long uId = JwtUtils.getUserId(jwtToken);
        for (String permission : permissions) {
            if (!authorityUtils.verify(uId, permission)) {
                return false;
            }
        }
        return true;
    }
}
