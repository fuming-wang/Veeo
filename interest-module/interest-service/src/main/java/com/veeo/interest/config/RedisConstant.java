package com.veeo.interest.config;


public class RedisConstant {

    public static final String USER_MODEL = "user:model:";

    /**
     * 用于兴趣推送时去重, 和下面的浏览记录存储数据结构不同, 这里只需要存id
     */
    public static final String HISTORY_VIDEO = "history:video:";

    /**
     * 系统视频库,每个公开的都会存在这, 这可能是一个大key
     */
    public static final String SYSTEM_STOCK = "system:stock:";

    /**
     * 系统分类库，用于查询分类下的视频随机获取, 这可能是一个大key
     */
    public static final String SYSTEM_TYPE_STOCK = "system:type:stock:";

    /**
     * 热门排行榜
     */
    public static final String HOT_RANK = "hot:rank";

    public static final Long HISTORY_TIME = 432000L;

    /**
     * 发件箱
     */
    public static final String OUT_FOLLOW = "out:follow:feed:";

    /**
     * 收件箱
     */
    public static final String IN_FOLLOW = "in:follow:feed:";

}
