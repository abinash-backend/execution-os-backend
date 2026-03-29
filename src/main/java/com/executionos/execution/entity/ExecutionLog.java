package com.executionos.execution.entity;

import com.executionos.common.util.ExecutionStatus;
import com.executionos.task.entity.Task;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "execution_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "date"})
)
@Getter
@Setter
public class ExecutionLog {

    @Id
    @GeneratedValue
    private UUID id;

    // Proper relation instead of raw UUID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    public ExecutionLog() {
    }

    public ExecutionLog(Task task, LocalDate date, ExecutionStatus status) {
        this.task = task;
        this.date = date;
        this.status = status;
    }
}

