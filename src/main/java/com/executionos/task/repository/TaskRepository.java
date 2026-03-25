package com.executionos.task.repository;

import com.executionos.common.util.Priority;
import com.executionos.common.util.Status;
import com.executionos.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByUserId(UUID userId);

    List<Task> findByUserIdAndStatus(UUID userId, Status status);

    List<Task> findByUserIdAndPriority(UUID userId, Priority priority);

    List<Task> findByUserIdAndStatusAndPriority(UUID userId, Status status, Priority priority);
}