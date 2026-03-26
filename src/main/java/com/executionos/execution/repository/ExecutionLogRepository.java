package com.executionos.execution.repository;

import com.executionos.execution.entity.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, UUID> {

    List<ExecutionLog> findByTaskIdOrderByDateDesc(UUID taskId);
}