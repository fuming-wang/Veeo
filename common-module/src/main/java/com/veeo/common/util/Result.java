package com.veeo.common.util;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;

@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 22L;

    /**
     * 响应码: 0 成功 1失败
     */
    private int code;

    private int errorCode;

    private String message;

    private T data;

    private long count;

    private Boolean state;


    public Result() {
    }

    public boolean isSuccess() {
        return code == 0;
    }

    public boolean isFailed() {
        return code != 0;
    }


    public Result<T> code(int code) {
        this.setCode(code);
        return this;
    }


    public Result<T> message(String message) {
        this.setMessage(message);
        return this;
    }

    public Result<T> message(String message, Object... objects) {
        this.setMessage(MessageFormat.format(message, objects));
        return this;
    }

    public Result<T> data(T result) {
        this.setData(result);
        return this;
    }

    public Result<T> count(long count) {
        this.setCount(count);
        return this;
    }

}
