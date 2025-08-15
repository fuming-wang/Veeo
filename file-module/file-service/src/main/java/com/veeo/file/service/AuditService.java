package com.veeo.file.service;

/**
 * @description: 用于处理审核
 */
public interface AuditService<T, R> {

    /**
     * 审核规范
     */
    R audit(T task);
}
