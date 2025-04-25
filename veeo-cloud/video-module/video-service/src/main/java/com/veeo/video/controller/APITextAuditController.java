package com.veeo.video.controller;

import com.veeo.common.entity.response.AuditResponse;
import com.veeo.video.api.TextAuditClient;
import com.veeo.video.service.audit.TextAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/textAudit")
public class APITextAuditController implements TextAuditClient {

    private final TextAuditService textAuditService;

    public APITextAuditController(TextAuditService textAuditService) {
        this.textAuditService = textAuditService;
    }

    @Override
    public AuditResponse audit(String text) {
        if (text == null) {
            log.warn("text is null or empty");
        }
        return textAuditService.audit(text);
    }
}
