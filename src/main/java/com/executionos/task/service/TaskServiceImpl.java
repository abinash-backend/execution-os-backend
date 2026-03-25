package com.executionos.task.service;

import com.executionos.auth.entity.User;
import com.executionos.auth.repository.UserRepository;
import com.executionos.common.util.Priority;
import com.executionos.common.util.Status;
import com.executionos.task.dto.TaskRequestDTO;
import com.executionos.task.dto.TaskResponseDTO;
import com.executionos.task.entity.Task;
import com.executionos.task.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskServiceImpl(TaskRepository taskRepository,
                           UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TaskResponseDTO createTask(TaskRequestDTO request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDeadline(request.getDeadline());
        task.setPriority(request.getPriority());
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
}