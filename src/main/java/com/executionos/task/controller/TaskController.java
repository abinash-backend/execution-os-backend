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

    // ✅ CREATE TASK
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(
            @RequestBody @Valid TaskRequestDTO request) {

        TaskResponseDTO response = taskService.createTask(request);
        return ResponseEntity.ok(response);
    }

    // ✅ GET TASKS (FILTERED)
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getTasks(
            @RequestParam UUID userId,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority) {

        List<TaskResponseDTO> tasks =
                taskService.getTasksByUser(userId, status, priority);

        return ResponseEntity.ok(tasks);
    }

    // ✅ STREAK
    @GetMapping("/{taskId}/streak")
    public ResponseEntity<StreakResponseDTO> getStreak(
            @PathVariable UUID taskId) {

        StreakResponseDTO response = taskService.calculateStreak(taskId);
        return ResponseEntity.ok(response);
    }

    // ✅ LEADERBOARD (PAGINATED)
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardResponseDTO>> getLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        List<LeaderboardResponseDTO> leaderboard =
                taskService.getLeaderboard(page, size);

        return ResponseEntity.ok(leaderboard);
    }
}