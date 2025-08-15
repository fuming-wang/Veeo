package com.veeo.video.exception;

import com.veeo.common.exception.BaseException;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.StringJoiner;

@RestController
@ControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(Exception.class)
    public Result<String> ex(Exception e) {
        log.error("异常信息 {}", e.getMessage(), e);
        String msg = ObjectUtils.isEmpty(e.getMessage()) ? e.toString() : e.getMessage();
        return ResultUtil.getFailRet(msg);
    }

    @ExceptionHandler(BaseException.class)
    public Result<String> bex(BaseException e) {
        log.error("BaseException 异常信息 {}", e.getMsg());
        return ResultUtil.getFailRet(e.getMsg());
    }


    // 数据校验异常处理
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> exception(MethodArgumentNotValidException e) {
        log.error("异常信息 {}", e.getMessage(), e);
        // e.getBindingResult()：获取BindingResult
        BindingResult bindingResult = e.getBindingResult();
        // 收集数据校验失败后的信息
        StringJoiner joiner = new StringJoiner(",");

        bindingResult.getFieldErrors().forEach((fieldError) -> {
            joiner.add(fieldError.getDefaultMessage());

        });
        return ResultUtil.getFailRet(joiner.toString());
    }
}

