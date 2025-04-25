package com.veeo.common.entity.video;

import com.veeo.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;



@Data
@EqualsAndHashCode(callSuper = false)
public class VideoShare extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long videoId;

    private Long userId;

    private String ip;

}
