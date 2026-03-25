package com.executionos.task.controller;

import com.executionos.task.dto.TaskRequestDTO;
import com.executionos.task.dto.TaskResponseDTO;
import com.executionos.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
}