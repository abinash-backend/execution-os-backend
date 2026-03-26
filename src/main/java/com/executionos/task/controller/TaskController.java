package com.executionos.task.controller;

import com.executionos.common.util.Priority;
import com.executionos.common.util.Status;
import com.executionos.task.dto.LeaderboardResponseDTO;
import com.executionos.task.dto.StreakResponseDTO;
import com.executionos.task.dto.TaskRequestDTO;
import com.executionos.task.dto.TaskResponseDTO;
import com.executionos.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public TaskResponseDTO createTask(@RequestBody @Valid TaskRequestDTO request) {
        return taskService.createTask(request);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getTasks(
            @RequestParam UUID userId,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority) {

        List<TaskResponseDTO> tasks =
                taskService.getTasksByUser(userId, status, priority);

        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}/streak")
    public ResponseEntity<StreakResponseDTO> getStreak(@PathVariable UUID taskId) {

        StreakResponseDTO response = taskService.calculateStreak(taskId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardResponseDTO>> getLeaderboard() {
        return ResponseEntity.ok(taskService.getLeaderboard());
    }
}