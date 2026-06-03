package com.interview.common.exception;

/**
 * 资源不存在异常 — HTTP 404
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String resource, Object id) {
        super(resource + " 不存在: " + id);
    }
}
