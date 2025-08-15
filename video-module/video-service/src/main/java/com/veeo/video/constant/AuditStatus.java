package com.veeo.video.constant;

import java.util.HashMap;
import java.util.Map;

public class AuditStatus {

    private static final Map<Integer, String> map = new HashMap<>();
    /**
     * 通过
     */
    public static Integer SUCCESS = 0; // 通过
    /**
     * 审核中
     */
    public static Integer PROCESS = 1;
    /**
     * 失败
     */
    public static Integer PASS = 2;
    /**
     * 需要人工审核
     */
    public static Integer MANUAL = 3;

    static {
        map.put(SUCCESS, "通过");
        map.put(PROCESS, "审核中");
        map.put(PASS, "失败");
        map.put(MANUAL, "需要人工审核");
    }

    public static String getName(Integer audit) {
        return map.get(audit);
    }
}
