package com.veeo.common.constant;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.user.UserSubscribe;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.video.Video;
import com.veeo.common.query.DataBase;

import java.util.HashMap;
import java.util.Map;


// 里面的字符可不可以用字符模型
public class CrossDataBaseConstant {


    public static final Map<String, SFunction<User, ?>> USER_FIELDS = new HashMap<>();
    public static final Map<String, SFunction<Video, ?>> VIDEO_FIELDS = new HashMap<>();
    public static final Map<String, SFunction<UserSubscribe, ?>> USER_SUBSCRIBE_FIELDS = new HashMap<>();
    public static final Map<String, SFunction<Type, ?>> TYPE_FIELDS = new HashMap<>();
    static {
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
        switch (dataBase) {
            case USER:
                return (Map<String, SFunction<T, ?>>) (Map<?, ?>) CrossDataBaseConstant.USER_FIELDS;
            case VIDEO:
                return (Map<String, SFunction<T, ?>>) (Map<?, ?>) CrossDataBaseConstant.VIDEO_FIELDS;
            case USER_SUBSCRIBE:
                return (Map<String, SFunction<T, ?>>) (Map<?, ?>) CrossDataBaseConstant.USER_SUBSCRIBE_FIELDS;
            case TYPE:
                return (Map<String, SFunction<T, ?>>) (Map<?, ?>) CrossDataBaseConstant.TYPE_FIELDS;
            default:
                throw new IllegalArgumentException("Unknown entity type: " + dataBase);
        }
    }


}
