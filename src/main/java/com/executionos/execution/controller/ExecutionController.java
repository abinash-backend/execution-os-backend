package com.executionos.execution.controller;

import com.executionos.execution.dto.ExecutionRequestDTO;
import com.executionos.execution.dto.ExecutionResponseDTO;
import com.executionos.execution.service.ExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    // ✅ MARK EXECUTION (SECURE)
    @PostMapping("/{taskId}/execution")
    public ResponseEntity<ExecutionResponseDTO> markExecution(
            @PathVariable UUID taskId,
            @RequestBody ExecutionRequestDTO request) {

        String userId = getCurrentUserId();

        ExecutionResponseDTO response =
                executionService.markExecution(taskId, request, userId);

        return ResponseEntity.status(201).body(response); // ✅ 201 CREATED
    }

    // ✅ GET LOGS (SECURE)
    @GetMapping("/{taskId}/execution")
    public ResponseEntity<List<ExecutionResponseDTO>> getLogs(
            @PathVariable UUID taskId) {

        String userId = getCurrentUserId();

        List<ExecutionResponseDTO> logs =
                executionService.getExecutionLogs(taskId, userId);

        return ResponseEntity.ok(logs);
    }

    // 🔐 COMMON METHOD
    private String getCurrentUserId() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }
}
