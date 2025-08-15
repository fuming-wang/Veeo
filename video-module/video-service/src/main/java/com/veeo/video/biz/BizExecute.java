package com.veeo.video.biz;

import com.veeo.video.config.BizContext;
import com.veeo.video.handler.BizHandler;

import java.util.List;

/**
 * @ClassName BizExecute
 * @Description
 * @Author wangfuming
 * @Date 2025/7/26 14:48
 * @Version 1.0.0
 */
public abstract class BizExecute {
    public void execute(List<BizHandler> bizHandlers, BizContext bizContext) {
        bizHandlers.forEach(bizHandler -> bizHandler.handle(bizContext));
    }
}
