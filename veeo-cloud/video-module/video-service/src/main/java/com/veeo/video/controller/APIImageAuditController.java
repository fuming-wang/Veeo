package com.veeo.video.controller;

import com.veeo.common.entity.response.AuditResponse;
import com.veeo.video.api.ImageAuditClient;
import com.veeo.video.service.audit.ImageAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequestMapping("/api/imageAudit")
public class APIImageAuditController implements ImageAuditClient {


    private final ImageAuditService imageAuditService;

    public APIImageAuditController(ImageAuditService imageAuditService) {
        this.imageAuditService = imageAuditService;
    }

    @Override
    public AuditResponse audit(String url) {
        if (url == null) {
            log.error("url is null or empty");
        }
        return imageAuditService.audit(url);
    }
}
