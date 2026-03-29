package com.executionos.execution.service;

import com.executionos.common.exception.ForbiddenException;
import com.executionos.common.exception.ResourceNotFoundException;
import com.executionos.execution.dto.ExecutionRequestDTO;
import com.executionos.execution.dto.ExecutionResponseDTO;
import com.executionos.execution.entity.ExecutionLog;
import com.executionos.execution.repository.ExecutionRepository;
import com.executionos.task.entity.Task;
import com.executionos.task.repository.TaskRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ExecutionService {

    private final ExecutionRepository executionRepository;
    private final TaskRepository taskRepository;

    public ExecutionService(ExecutionRepository executionRepository,
                            TaskRepository taskRepository) {
        this.executionRepository = executionRepository;
        this.taskRepository = taskRepository;
    }

    // MARK EXECUTION
    public ExecutionResponseDTO markExecution(UUID taskId,
                                              ExecutionRequestDTO request,
                                              String userId) {

        // 1. Validate task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // 2. Ownership validation
        if (!task.getUser().getId().toString().equals(userId)) {
            throw new ForbiddenException("You do not own this task");
        }

        LocalDate today = LocalDate.now();

        // 3. Duplicate check
        boolean exists = executionRepository.existsByTaskAndDate(task, today);

        if (exists) {
            throw new RuntimeException("Execution already logged for today");
        }

        // 4. Save execution
        ExecutionLog log = new ExecutionLog();
        log.setTask(task);        // ← relation instead of UUID
        log.setDate(today);
        log.setStatus(request.getStatus());

        try {
            executionRepository.save(log);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Execution already exists for today");
        }

        // 5. Build response
        ExecutionResponseDTO response = new ExecutionResponseDTO();
        response.setDate(log.getDate());
        response.setStatus(log.getStatus());

        return response;
    }

    // GET EXECUTION LOGS
    public List<ExecutionResponseDTO> getExecutionLogs(UUID taskId,
                                                       String userId) {

        // Validate task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Ownership validation
        if (!task.getUser().getId().toString().equals(userId)) {
            throw new ForbiddenException("You do not own this task");
        }

        return executionRepository.findByTask(task)
                .stream()
                .map(log -> {
                    ExecutionResponseDTO dto = new ExecutionResponseDTO();
                    dto.setDate(log.getDate());
                    dto.setStatus(log.getStatus());
                    return dto;
                })
                .toList();
    }
}
