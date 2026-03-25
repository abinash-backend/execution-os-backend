package com.executionos.task.service;

import com.executionos.task.dto.TaskRequestDTO;
import com.executionos.task.dto.TaskResponseDTO;

public interface TaskService {

    TaskResponseDTO createTask(TaskRequestDTO request);
}