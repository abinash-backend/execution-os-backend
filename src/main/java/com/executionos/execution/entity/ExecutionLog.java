package com.executionos.execution.entity;

import com.executionos.task.entity.Task;
import com.executionos.common.util.ExecutionStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "execution_logs")
@Data
public class ExecutionLog {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;
}