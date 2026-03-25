package com.executionos.task.dto;

import com.executionos.common.util.Priority;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class TaskRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private LocalDate deadline;

    private Priority priority;

    private UUID userId;
}