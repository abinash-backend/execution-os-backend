package com.executionos.task.service;

import com.executionos.auth.entity.User;
import com.executionos.auth.repository.UserRepository;
import com.executionos.common.exception.DuplicateResourceException;
import com.executionos.common.exception.ResourceNotFoundException;
import com.executionos.common.util.ExecutionStatus;
import com.executionos.common.util.Priority;
import com.executionos.common.util.Status;
import com.executionos.execution.entity.ExecutionLog;
import com.executionos.task.dto.LeaderboardResponseDTO;
import com.executionos.task.dto.StreakResponseDTO;
import com.executionos.task.dto.TaskRequestDTO;
import com.executionos.task.dto.TaskResponseDTO;
import com.executionos.task.entity.Task;
import com.executionos.task.repository.TaskRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.executionos.execution.repository.ExecutionLogRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ExecutionLogRepository executionLogRepository;
    public TaskServiceImpl(TaskRepository taskRepository,
                           UserRepository userRepository, ExecutionLogRepository executionLogRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.executionLogRepository = executionLogRepository;
    }

    @Override
    public TaskResponseDTO createTask(TaskRequestDTO request, UUID userId) {
        String normalizedTitle = request.getTitle().trim();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (taskRepository.existsByUserIdAndTitleIgnoreCase(userId, normalizedTitle)) {
            throw new DuplicateResourceException("Task with the same title already exists");
        }

        Task task = new Task();
        task.setTitle(normalizedTitle);
        task.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        task.setFrequency(request.getFrequency());
        task.setDeadline(request.getDeadline());
        task.setPriority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM);
        task.setStatus(Status.PENDING);
        task.setUser(user);

        Task saved = taskRepository.save(task);

        return mapToDTO(saved);
    }

    @Override
    public List<TaskResponseDTO> getTasksByUser(UUID userId) {

        List<Task> tasks = taskRepository.findByUserId(userId);

        return tasks.stream()
                .map(this::mapToDTO)
                .toList();
    }

    private TaskResponseDTO mapToDTO(Task task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setFrequency(task.getFrequency());
        dto.setDeadline(task.getDeadline());
        dto.setPriority(task.getPriority());
        dto.setStatus(task.getStatus());
        dto.setCreatedAt(task.getCreatedAt());
        return dto;
    }

    @Override
    public List<TaskResponseDTO> getTasksByUser(UUID userId, Status status, Priority priority) {

        List<Task> tasks;

        if (status != null && priority != null) {
            tasks = taskRepository.findByUserIdAndStatusAndPriority(userId, status, priority);

        } else if (status != null) {
            tasks = taskRepository.findByUserIdAndStatus(userId, status);

        } else if (priority != null) {
            tasks = taskRepository.findByUserIdAndPriority(userId, priority);

        } else {
            tasks = taskRepository.findByUserId(userId);
        }

        return tasks.stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public StreakResponseDTO calculateStreak(UUID taskId) {

        List<ExecutionLog> logs =
                executionLogRepository.findByTaskIdOrderByDateDesc(taskId);

        if (logs.isEmpty()) {
            return new StreakResponseDTO(taskId, 0, 0, 0);
        }

        int currentStreak = 0;
        int longestStreak = 0;
        int tempStreak = 0;
        int doneCount = 0;

        LocalDate expectedDate = LocalDate.now();

        // ✅ CURRENT STREAK
        for (ExecutionLog log : logs) {
            if (!log.getDate().equals(expectedDate)) break;
            if (log.getStatus() == ExecutionStatus.MISSED) break;

            currentStreak++;
            expectedDate = expectedDate.minusDays(1);
        }

        // ✅ LONGEST STREAK + DONE COUNT
        for (ExecutionLog log : logs) {

            if (log.getStatus() == ExecutionStatus.DONE) {
                tempStreak++;
                doneCount++;
                longestStreak = Math.max(longestStreak, tempStreak);
            } else {
                tempStreak = 0;
            }
        }

        // ✅ CONSISTENCY
        double consistency = ((double) doneCount / logs.size()) * 100;

        return new StreakResponseDTO(
                taskId,
                currentStreak,
                longestStreak,
                consistency
        );
    }

    @Override
    public List<LeaderboardResponseDTO> getLeaderboard(org.springframework.data.domain.Pageable pageable) {
        return List.of();
    }

    @Override
    public List<LeaderboardResponseDTO> getLeaderboard() {

        List<User> users = userRepository.findAll();
        List<LeaderboardResponseDTO> leaderboard = new ArrayList<>();

        for (User user : users) {

            List<Task> tasks = taskRepository.findByUserId(user.getId());
            if (tasks.isEmpty()) continue;

            double totalConsistency = 0;
            int count = 0;

            for (Task task : tasks) {

                List<ExecutionLog> logs =
                        executionLogRepository.findByTaskIdOrderByDateDesc(task.getId());

                if (logs.isEmpty()) continue;

                int done = 0;

                for (ExecutionLog log : logs) {
                    if (log.getStatus() == ExecutionStatus.DONE) {
                        done++;
                    }
                }

                double consistency = ((double) done / logs.size()) * 100;

                totalConsistency += consistency;
                count++;
            }

            if (count == 0) continue;

            double avgConsistency = totalConsistency / count;

            leaderboard.add(new LeaderboardResponseDTO(
                    user.getId(),
                    avgConsistency
            ));
        }

        leaderboard.sort((a, b) ->
                Double.compare(b.getConsistencyScore(), a.getConsistencyScore())
        );

        return leaderboard;
    }

    @Override
    public List<LeaderboardResponseDTO> getLeaderboard(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        List<User> users = userRepository.findAll(pageable).getContent();

        List<LeaderboardResponseDTO> leaderboard = new ArrayList<>();

        for (User user : users) {

            List<Task> tasks = taskRepository.findByUserId(user.getId());
            if (tasks.isEmpty()) continue;

            double totalConsistency = 0;
            int count = 0;

            for (Task task : tasks) {

                List<ExecutionLog> logs =
                        executionLogRepository.findByTaskIdOrderByDateDesc(task.getId());

                if (logs.isEmpty()) continue;

                int done = 0;

                for (ExecutionLog log : logs) {
                    if (log.getStatus() == ExecutionStatus.DONE) {
                        done++;
                    }
                }

                double consistency = ((double) done / logs.size()) * 100;

                totalConsistency += consistency;
                count++;
            }

            if (count == 0) continue;

            double avg = totalConsistency / count;

            leaderboard.add(new LeaderboardResponseDTO(user.getId(), avg));
        }

        leaderboard.sort((a, b) ->
                Double.compare(b.getConsistencyScore(), a.getConsistencyScore())
        );

        return leaderboard;
    }
}
