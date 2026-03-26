package com.executionos.execution.dto;

import com.executionos.common.util.ExecutionStatus;
import java.time.LocalDate;

public class ExecutionResponseDTO {

    private LocalDate date;
    private ExecutionStatus status;

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public ExecutionStatus getStatus() {
        return this.status;
    }
}