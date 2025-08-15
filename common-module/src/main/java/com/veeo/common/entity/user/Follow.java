package com.veeo.common.entity.user;

import com.baomidou.mybatisplus.annotation.*;
import com.veeo.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = false)
public class Follow extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 关注id */
    private Long followId;

    /** 粉丝id */
    private Long userId;

}
