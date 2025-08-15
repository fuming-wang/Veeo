package com.veeo.common.entity.vo;

import com.veeo.common.entity.user.Follow;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
public class FollowVO extends Follow {

    private String nickName;
}
