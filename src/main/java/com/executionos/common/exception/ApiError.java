package com.executionos.common.exception;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ApiError {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String method;
    private String path;

    public ApiError(int status, String error, String method, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.method = method;
        this.path = path;
    }
}