package com.veeo.common.util;

/**
 * @ClassName ResultUtil
 * @Description
 * @Author wangfuming
 * @Date 2025/7/14 22:07
 * @Version 1.0.0
 */
public class ResultUtil {

    public static <T> Result<T> getSucRet(String msg) {
        Result<T> response = new Result<>();
        response.setState(true);
        response.setCode(0);
        response.setMessage(msg);
        return response;
    }

    public static <T> Result<T> getSucRet() {
        Result<T> response = new Result<>();
        response.setState(true);
        response.setCode(0);
        response.setMessage("success");
        return response;
    }

    public static <T> Result<T> getSucRet(T data) {
        Result<T> response = new Result<>();
        response.setCode(0);
        response.setMessage("成功");
        response.setData(data);
        response.setState(true);
        return response;
    }

    public static <T> Result<T> getFailRet(int errorCode, String errorMsg) {
        Result<T> response = new Result<>();
        response.setCode(1);
        response.setMessage(errorMsg);
        response.setErrorCode(errorCode);
        response.setState(false);
        return response;
    }

    public static <T> Result<T> getFailRet(String errorMsg) {
        Result<T> response = new Result<>();
        response.setCode(1);
        response.setErrorCode(0);
        response.setMessage(errorMsg);
        response.setState(false);
        return response;
    }
}
