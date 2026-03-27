package com.executionos.task.service;

import com.executionos.common.util.Priority;
import com.executionos.common.util.Status;
import com.executionos.task.dto.LeaderboardResponseDTO;
import com.executionos.task.dto.StreakResponseDTO;
import com.executionos.task.dto.TaskRequestDTO;
import com.executionos.task.dto.TaskResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskResponseDTO createTask(TaskRequestDTO request, UUID userId);

    List<TaskResponseDTO> getTasksByUser(UUID userId);

    List<TaskResponseDTO> getTasksByUser(UUID userId, Status status, Priority priority);

    StreakResponseDTO calculateStreak(UUID taskId);

    // ✅ SINGLE CLEAN METHOD
    List<LeaderboardResponseDTO> getLeaderboard(Pageable pageable);

    List<LeaderboardResponseDTO> getLeaderboard();

    List<LeaderboardResponseDTO> getLeaderboard(int page, int size);
}
