package com.executionos.task.service;

import com.executionos.common.util.Priority;
import com.executionos.common.util.Status;
import com.executionos.task.dto.LeaderboardResponseDTO;
import com.executionos.task.dto.TaskRequestDTO;
import com.executionos.task.dto.TaskResponseDTO;
import com.executionos.task.dto.StreakResponseDTO;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskResponseDTO createTask(TaskRequestDTO request);

    List<TaskResponseDTO> getTasksByUser(UUID userId);

    List<TaskResponseDTO> getTasksByUser(UUID userId, Status status, Priority priority);

    StreakResponseDTO calculateStreak(UUID taskId);

    List<LeaderboardResponseDTO> getLeaderboard();
}