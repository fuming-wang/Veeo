package com.veeo.user.constant;


public class RedisConstant {

    /** 邮箱验证码保存时间（5分钟）*/
    public static final Long EMAIL_CODE_TIME = 300L;

    /** 用户搜素记录保持时间 */
    public static final Long USER_SEARCH_HISTORY_TIME = 432000L;

    /** 注册验证码 */
    public static final String EMAIL_CODE = "email:code:";

    /** 用户搜索记录 */
    public static final String USER_SEARCH_HISTORY = "user:search:history:";

    /** 用户关注人 */
    public static final String USER_FOLLOW = "user:follow:";

    /** 用户关注数缓存 */
    public static final String USER_FOLLOW_NUMBER_CACHE = "user:follow:number:cache:";

    /** 用户粉丝 */
    public static final String USER_FANS = "user:fans:";

    /** 用户粉丝数缓存 */
    public static final String USER_FANS_NUMBER_CACHE = "user:fans:number:cache:";

    /** 用户信息缓存 */
    public static final String USER_CACHE = "user:cache:";

    /** 验证码临时存储 */
    public static final String CAPTCHA_CACHE = "captcha:cache:";

}
