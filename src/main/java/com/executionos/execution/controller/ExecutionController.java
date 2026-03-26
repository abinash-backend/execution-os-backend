package com.executionos.execution.controller;

import com.executionos.execution.dto.ExecutionRequestDTO;
import com.executionos.execution.dto.ExecutionResponseDTO;
import com.executionos.execution.service.ExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class ExecutionController {

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    // ✅ Mark execution
    @PostMapping("/{taskId}/execution")
    public ResponseEntity<?> markExecution(
            @PathVariable UUID taskId,
            @RequestBody ExecutionRequestDTO request) {

        executionService.markExecution(taskId, request);
        return ResponseEntity.ok("Execution recorded");
    }

    // ✅ Get logs
    @GetMapping("/{taskId}/execution")
    public ResponseEntity<List<ExecutionResponseDTO>> getLogs(
            @PathVariable UUID taskId) {

        return ResponseEntity.ok(
                executionService.getExecutionLogs(taskId)
        );
    }
}