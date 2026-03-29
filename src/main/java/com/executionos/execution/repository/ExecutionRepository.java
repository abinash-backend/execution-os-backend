package com.executionos.execution.repository;

import com.executionos.execution.entity.ExecutionLog;
import com.executionos.task.entity.Task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<ExecutionLog, UUID> {

    boolean existsByTaskAndDate(Task task, LocalDate date);

    List<ExecutionLog> findByTask(Task task);
}

