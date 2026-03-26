package com.executionos.execution.entity;

import com.executionos.common.util.ExecutionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "execution_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "date"}))
public class ExecutionLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Setter
    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Getter
    @Setter
    @Column(nullable = false)
    private LocalDate date;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    // constructors, getters
}