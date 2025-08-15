package com.veeo.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class AuthorityException extends Exception {

    private int code;

    private String msg;

    public AuthorityException(String msg){
        super(msg);
    }
}
