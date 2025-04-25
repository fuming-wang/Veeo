package com.veeo.common.authority;

import com.veeo.common.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component(value = "postMappingAuthorityVerify")
public class UnifyAuthorityVerify extends DefaultAuthorityVerify{

    @Autowired
    private AuthorityUtils authorityUtils;


    @Override
    public Boolean authorityVerify(HttpServletRequest request, String... permissions) {
        String jwtToken = request.getHeader("token");
        Long uId = JwtUtils.getUserId(jwtToken);
        for (String permission : permissions) {
            if (!authorityUtils.verify(uId,permission)) {
                return false;
            }
        }
        return true;
    }
}
