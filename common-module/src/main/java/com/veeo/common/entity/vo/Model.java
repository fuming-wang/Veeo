package com.veeo.common.entity.vo;

import lombok.Data;


@Data
public class Model {

    /** 标签 */
    private String labels;

    /** 视频id */
    private Long id;

    /** 分数 */
    private Double score;
}
