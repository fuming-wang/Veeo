package com.veeo.common.authority;


import jakarta.servlet.http.HttpServletRequest;

public interface AuthorityVerify {

    Boolean authorityVerify(HttpServletRequest request, String[] permissions);
}
