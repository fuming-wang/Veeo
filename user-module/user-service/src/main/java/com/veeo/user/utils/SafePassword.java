package com.veeo.user.utils;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class SafePassword {

    private static final String SALT = "$2a$10$WN.h2KCJKLHDF2S6yuAAhe";

    public static String safeHash(String password) {
        return BCrypt.hashpw(password, SALT);
    }

    public static boolean verify(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }

    public static void main(String[] args) {
        System.out.println(safeHash("123456"));
        System.out.println(safeHash("official_user1"));
        System.out.println(safeHash("veeo_admin"));
        System.out.println(safeHash("veeo_check"));
        System.out.println(safeHash("fmwang@111.com"));
    }

}
