package com.veeo.common.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.util.ObjectUtils;

import java.util.Date;


public class JwtUtils {

    /**
     * token过期时间
     */
    public static final long EXPIRE = 10000000L * 60 * 60 * 24;

    /**
     * 秘钥
     */
    public static final String APP_SECRET = "2pHkZnZW88avJSMACfe3fQJwe7yrkS6Y"; //

    /**
     * 生成token字符串的方法
     **/
    public static String getJwtToken(Long id, String nickname) {

        //设置token主体部分 ，存储用户信息
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setSubject("guli-user")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .claim("id", String.valueOf(id))  //设置token主体部分 ，存储用户信息
                .claim("nickname", nickname)
                .signWith(SignatureAlgorithm.HS256, APP_SECRET)
                .compact();
    }

    /**
     * 判断token是否存在与有效
     */
    public static boolean checkToken(String jwtToken) {
        try {
            Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(jwtToken);
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    /**
     * 该方法使用在各个微服务内
     */
    public static Long getUserId(String jwtToken) {
        if (ObjectUtils.isEmpty(jwtToken)) {
            return null;
        }
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(jwtToken);
        Claims claims = claimsJws.getBody();
        return Long.valueOf(claims.get("id").toString());
    }


}
