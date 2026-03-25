package com.executionos.task.dto;

import com.executionos.common.util.Priority;
import com.executionos.common.util.Status;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaskResponseDTO {

    private UUID id;
    private String title;
    private String description;
    private LocalDate deadline;
    private Priority priority;
    private Status status;
    private LocalDateTime createdAt;
}