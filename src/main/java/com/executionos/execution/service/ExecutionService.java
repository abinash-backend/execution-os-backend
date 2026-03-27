package com.executionos.execution.service;

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

    // ✅ MARK EXECUTION (SECURE + SAFE)
    public ExecutionResponseDTO markExecution(UUID taskId,
                                              ExecutionRequestDTO request,
                                              String userId) {

        // ✅ 1. Validate Task Exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // ✅ 2. Ownership Validation (CRITICAL)
        if (!task.getUser().getId().toString().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        LocalDate today = LocalDate.now();

        // ⚠️ 3. Duplicate Check (still needed, but DB is final authority)
        boolean exists = executionRepository
                .existsByTaskIdAndDate(taskId, today);

        if (exists) {
            throw new RuntimeException("Execution already logged for today");
        }

        // ✅ 4. Save Execution
        ExecutionLog log = new ExecutionLog();
        log.setTaskId(taskId); // (we will improve later → @ManyToOne)
        log.setDate(today);
        log.setStatus(request.getStatus());

        try {
            executionRepository.save(log);
        } catch (DataIntegrityViolationException e) {
            // ✅ Handles race condition safely
            throw new RuntimeException("Execution already exists for today");
        }

        // ✅ 5. Return Response (important)
        ExecutionResponseDTO response = new ExecutionResponseDTO();
        response.setDate(log.getDate());
        response.setStatus(log.getStatus());

        return response;
    }

    // ✅ GET LOGS (SECURE)
    public List<ExecutionResponseDTO> getExecutionLogs(UUID taskId,
                                                       String userId) {

        // ✅ Validate Task Exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // ✅ Ownership Check
        if (!task.getUser().getId().toString().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        return executionRepository.findByTaskId(taskId)
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
