package com.executionos.task.service;

import com.executionos.auth.entity.User;
import com.executionos.auth.repository.UserRepository;
import com.executionos.common.exception.DuplicateResourceException;
import com.executionos.common.util.Frequency;
import com.executionos.common.util.Priority;
import com.executionos.common.util.Status;
import com.executionos.execution.repository.ExecutionLogRepository;
import com.executionos.task.dto.TaskRequestDTO;
import com.executionos.task.dto.TaskResponseDTO;
import com.executionos.task.entity.Task;
import com.executionos.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExecutionLogRepository executionLogRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void createTaskSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);

        TaskRequestDTO request = new TaskRequestDTO();
        request.setTitle("  Daily Planning  ");
        request.setDescription("  Review priorities ");
        request.setFrequency(Frequency.DAILY);
        request.setDeadline(LocalDate.of(2026, 4, 30));
        request.setPriority(Priority.HIGH);

        Task savedTask = buildTask(
                UUID.randomUUID(),
                "Daily Planning",
                "Review priorities",
                Frequency.DAILY,
                LocalDate.of(2026, 4, 30),
                Priority.HIGH,
                Status.PENDING,
                user
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.existsByUserIdAndTitleIgnoreCase(userId, "Daily Planning")).thenReturn(false);
        when(taskRepository.save(org.mockito.ArgumentMatchers.any(Task.class))).thenReturn(savedTask);

        // Act
        TaskResponseDTO response = taskService.createTask(request, userId);

        // Assert
        assertNotNull(response);
        assertEquals(savedTask.getId(), response.getId());
        assertEquals("Daily Planning", response.getTitle());
        assertEquals("Review priorities", response.getDescription());
        assertEquals(Frequency.DAILY, response.getFrequency());
        assertEquals(Priority.HIGH, response.getPriority());
        assertEquals(Status.PENDING, response.getStatus());
        verify(userRepository).findById(userId);
        verify(taskRepository).existsByUserIdAndTitleIgnoreCase(userId, "Daily Planning");
        verify(taskRepository).save(org.mockito.ArgumentMatchers.any(Task.class));
    }

    @Test
    void createTaskShouldPreventDuplicateTaskTitles() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);
        TaskRequestDTO request = new TaskRequestDTO();
        request.setTitle("  Daily Planning  ");
        request.setFrequency(Frequency.DAILY);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.existsByUserIdAndTitleIgnoreCase(userId, "Daily Planning")).thenReturn(true);

        // Act
        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> taskService.createTask(request, userId)
        );

        // Assert
        assertEquals("Task with the same title already exists", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(taskRepository).existsByUserIdAndTitleIgnoreCase(userId, "Daily Planning");
        verify(taskRepository, never()).save(org.mockito.ArgumentMatchers.any(Task.class));
    }

    @Test
    void getTasksWithFiltersShouldUseStatusAndPriorityRepositoryQuery() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Task task = buildTask(
                UUID.randomUUID(),
                "Workout",
                "Morning session",
                Frequency.DAILY,
                LocalDate.of(2026, 4, 20),
                Priority.HIGH,
                Status.DONE,
                buildUser(userId)
        );

        when(taskRepository.findByUserIdAndStatusAndPriority(userId, Status.DONE, Priority.HIGH))
                .thenReturn(List.of(task));

        // Act
        List<TaskResponseDTO> responses = taskService.getTasksByUser(userId, Status.DONE, Priority.HIGH);

        // Assert
        assertEquals(1, responses.size());
        assertEquals(task.getId(), responses.get(0).getId());
        assertEquals("Workout", responses.get(0).getTitle());
        assertEquals(Status.DONE, responses.get(0).getStatus());
        assertEquals(Priority.HIGH, responses.get(0).getPriority());
        verify(taskRepository).findByUserIdAndStatusAndPriority(userId, Status.DONE, Priority.HIGH);
    }

    @Test
    void getTasksForAuthenticatedUserShouldReturnOnlyThatUsersTasks() {
        // Arrange
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId);
        Task firstTask = buildTask(
                UUID.randomUUID(),
                "Read",
                "Read 20 pages",
                Frequency.DAILY,
                LocalDate.of(2026, 4, 18),
                Priority.MEDIUM,
                Status.PENDING,
                user
        );
        Task secondTask = buildTask(
                UUID.randomUUID(),
                "Code",
                "Finish service tests",
                Frequency.DAILY,
                LocalDate.of(2026, 4, 19),
                Priority.HIGH,
                Status.DONE,
                user
        );

        when(taskRepository.findByUserId(userId)).thenReturn(List.of(firstTask, secondTask));

        // Act
        List<TaskResponseDTO> responses = taskService.getTasksByUser(userId);

        // Assert
        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(response -> response.getId() != null));
        assertEquals(List.of("Read", "Code"), responses.stream().map(TaskResponseDTO::getTitle).toList());
        verify(taskRepository).findByUserId(userId);
    }

    private User buildUser(UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail("user@example.com");
        return user;
    }

    private Task buildTask(
            UUID taskId,
            String title,
            String description,
            Frequency frequency,
            LocalDate deadline,
            Priority priority,
            Status status,
            User user
    ) {
        Task task = new Task();
        task.setId(taskId);
        task.setTitle(title);
        task.setDescription(description);
        task.setFrequency(frequency);
        task.setDeadline(deadline);
        task.setPriority(priority);
        task.setStatus(status);
        task.setUser(user);
        task.setCreatedAt(LocalDateTime.of(2026, 4, 15, 10, 0));
        return task;
    }
}
