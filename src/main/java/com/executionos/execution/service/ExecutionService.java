package com.executionos.execution.service;

import com.executionos.execution.dto.ExecutionRequestDTO;
import com.executionos.execution.dto.ExecutionResponseDTO;
import com.executionos.execution.entity.ExecutionLog;
import com.executionos.execution.repository.ExecutionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ExecutionService {

    private final ExecutionRepository executionRepository;

    public ExecutionService(ExecutionRepository executionRepository) {
        this.executionRepository = executionRepository;
    }

    public void markExecution(UUID taskId, ExecutionRequestDTO request) {

        LocalDate today = LocalDate.now();

        // ✅ Check duplicate
        boolean exists = executionRepository
                .existsByTaskIdAndDate(taskId, today);

        if (exists) {
            throw new RuntimeException("Execution already logged for today");
        }

        // ✅ Save execution
        ExecutionLog log = new ExecutionLog();
        log.setTaskId(taskId);
        log.setDate(today);
        log.setStatus(request.getStatus());

        executionRepository.save(log);
    }

    public List<ExecutionResponseDTO> getExecutionLogs(UUID taskId) {

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