package com.executionos.execution.dto;

import com.executionos.common.util.ExecutionStatus;

public class ExecutionRequestDTO {

    private ExecutionStatus status;

    public ExecutionStatus getStatus() {
        return this.status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }
}