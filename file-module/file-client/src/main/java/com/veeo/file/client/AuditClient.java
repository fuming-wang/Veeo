package com.veeo.file.client;

import com.veeo.common.entity.response.AuditResponse;
import com.veeo.common.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @ClassName AuditClient
 * @Description
 * @Author wangfuming
 * @Date 2025/7/29 21:56
 * @Version 1.0.0
 */
@FeignClient(value = "file-service", contextId = "audit-client", path = "/api/audit")
public interface AuditClient {

    @RequestMapping("/imageAudit")
    Result<AuditResponse> imageAudit(@RequestParam String url);

    @RequestMapping("/textAudit")
    Result<AuditResponse> textAudit(@RequestParam String text);

    @RequestMapping("/videoAudit")
    Result<AuditResponse> videoAudit(@RequestParam String url);
}
