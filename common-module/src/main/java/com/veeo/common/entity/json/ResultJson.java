package com.veeo.common.entity.json;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;


@Data
@ToString
public class ResultJson implements Serializable {
    Integer code;
    String message;
    ResultChildJson result;
}


