package com.veeo.common.constant;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.user.UserSubscribe;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.video.Video;
import com.veeo.common.query.DataBase;

import java.util.HashMap;
import java.util.Map;


/**
 * 跨服务数据库查询常量
 */
public class CrossDataBaseConstant {

    /** User表 字段string和Lambda表达式查询字典 */
    public static final Map<String, SFunction<User, ?>> USER_FIELDS = new HashMap<>();

    /** Video表 字段string和Lambda表达式查询字典 */
    public static final Map<String, SFunction<Video, ?>> VIDEO_FIELDS = new HashMap<>();

    /** UserSubscribe表 字段string和Lambda表达式查询字典 */
    public static final Map<String, SFunction<UserSubscribe, ?>> USER_SUBSCRIBE_FIELDS = new HashMap<>();

    /** Type表 字段string和Lambda表达式查询字典 */
    public static final Map<String, SFunction<Type, ?>> TYPE_FIELDS = new HashMap<>();

    static {
        /*
         * todo 里面的字符可不可以用常量替换
         */
        USER_FIELDS.put("id", User::getId);
        USER_FIELDS.put("nickName", User::getNickName);
        USER_FIELDS.put("email", User::getEmail);

        USER_SUBSCRIBE_FIELDS.put("userId", UserSubscribe::getUserId);

        TYPE_FIELDS.put("id", Type::getId);
        TYPE_FIELDS.put("name", Type::getName);
        TYPE_FIELDS.put("icon", Type::getIcon);
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<String, SFunction<T, ?>> getFieldsForEntity(DataBase dataBase) {
        return switch (dataBase) {
            case USER -> (Map<String, SFunction<T, ?>>) (Map<?, ?>) CrossDataBaseConstant.USER_FIELDS;
            case VIDEO -> (Map<String, SFunction<T, ?>>) (Map<?, ?>) CrossDataBaseConstant.VIDEO_FIELDS;
            case USER_SUBSCRIBE -> (Map<String, SFunction<T, ?>>) (Map<?, ?>) CrossDataBaseConstant.USER_SUBSCRIBE_FIELDS;
            case TYPE -> (Map<String, SFunction<T, ?>>) (Map<?, ?>) CrossDataBaseConstant.TYPE_FIELDS;
            default -> throw new IllegalArgumentException("Unknown entity type: " + dataBase);
        };
    }


}
