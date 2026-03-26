package com.executionos.execution.repository;

import com.executionos.execution.entity.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExecutionRepository extends JpaRepository<ExecutionLog, UUID> {

    boolean existsByTaskIdAndDate(UUID taskId, LocalDate date);

    List<ExecutionLog> findByTaskId(UUID taskId);
}