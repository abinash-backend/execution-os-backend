package com.executionos.task.service;

import com.executionos.task.dto.TaskRequestDTO;
import com.executionos.task.dto.TaskResponseDTO;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskResponseDTO createTask(TaskRequestDTO request);
    List<TaskResponseDTO> getTasksByUser(UUID userId);
}