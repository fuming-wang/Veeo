package com.veeo.file.api;

import com.veeo.common.entity.response.AuditResponse;
import com.veeo.common.util.Result;
import com.veeo.common.util.ResultUtil;
import com.veeo.file.client.AuditClient;
import com.veeo.file.service.audit.ImageAuditService;
import com.veeo.file.service.audit.TextAuditService;
import com.veeo.file.service.audit.VideoAuditService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName AuditApi
 * @Description
 * @Author wangfuming
 * @Date 2025/7/29 21:59
 * @Version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/audit")
public class AuditApi implements AuditClient {

    @Resource
    private ImageAuditService imageAuditService;

    @Resource
    private TextAuditService textAuditService;

    @Resource
    private VideoAuditService videoAuditService;

    @Override
    public Result<AuditResponse> imageAudit(String url) {
        return ResultUtil.getSucRet(imageAuditService.audit(url));
    }

    @Override
    public Result<AuditResponse> textAudit(String text) {
        return ResultUtil.getSucRet(textAuditService.audit(text));
    }

    @Override
    public Result<AuditResponse> videoAudit(String url) {
        return ResultUtil.getSucRet(videoAuditService.audit(url));
    }
}
