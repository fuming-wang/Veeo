package com.veeo.video.api;

import com.veeo.common.entity.response.AuditResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "video-service", contextId = "textAudit-client", path = "/api/textAudit")
public interface TextAuditClient {


    @RequestMapping("/textAudit")
    AuditResponse audit(@RequestParam String text);
}
