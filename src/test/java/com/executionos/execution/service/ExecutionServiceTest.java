package com.executionos.execution.service;

import com.executionos.auth.entity.User;
import com.executionos.auth.repository.UserRepository;
import com.executionos.common.exception.ForbiddenException;
import com.executionos.common.util.ExecutionStatus;
import com.executionos.execution.dto.ExecutionRequestDTO;
import com.executionos.execution.dto.ExecutionResponseDTO;
import com.executionos.execution.entity.ExecutionLog;
import com.executionos.execution.repository.ExecutionLogRepository;
import com.executionos.execution.repository.ExecutionRepository;
import com.executionos.task.entity.Task;
import com.executionos.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutionServiceTest {

    @Mock
    private ExecutionRepository executionRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExecutionLogRepository executionLogRepository;

    @InjectMocks
    private ExecutionService executionService;

    @Test
    void logExecutionSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Task task = buildTask(taskId, userId);
        ExecutionRequestDTO request = new ExecutionRequestDTO();
        request.setStatus(ExecutionStatus.DONE);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(executionRepository.existsByTaskAndDate(task, LocalDate.now())).thenReturn(false);
        when(executionRepository.save(any(ExecutionLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ExecutionResponseDTO response = executionService.markExecution(taskId, request, userId.toString());

        // Assert
        assertNotNull(response);
        assertEquals(LocalDate.now(), response.getDate());
        assertEquals(ExecutionStatus.DONE, response.getStatus());

        ArgumentCaptor<ExecutionLog> logCaptor = ArgumentCaptor.forClass(ExecutionLog.class);
        verify(taskRepository).findById(taskId);
        verify(executionRepository).existsByTaskAndDate(task, LocalDate.now());
        verify(executionRepository).save(logCaptor.capture());
        assertEquals(task, logCaptor.getValue().getTask());
        assertEquals(LocalDate.now(), logCaptor.getValue().getDate());
        assertEquals(ExecutionStatus.DONE, logCaptor.getValue().getStatus());
    }

    @Test
    void logExecutionShouldPreventDuplicateExecutionLogForSameDay() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Task task = buildTask(taskId, userId);
        ExecutionRequestDTO request = new ExecutionRequestDTO();
        request.setStatus(ExecutionStatus.DONE);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(executionRepository.existsByTaskAndDate(task, LocalDate.now())).thenReturn(true);

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> executionService.markExecution(taskId, request, userId.toString())
        );

        // Assert
        assertEquals("Execution already logged for today", exception.getMessage());
        verify(taskRepository).findById(taskId);
        verify(executionRepository).existsByTaskAndDate(task, LocalDate.now());
        verify(executionRepository, never()).save(any(ExecutionLog.class));
    }

    @Test
    void logExecutionShouldRejectExecutionForTaskNotOwnedByUser() {
        // Arrange
        UUID ownerId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Task task = buildTask(taskId, ownerId);
        ExecutionRequestDTO request = new ExecutionRequestDTO();
        request.setStatus(ExecutionStatus.DONE);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        // Act
        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> executionService.markExecution(taskId, request, anotherUserId.toString())
        );

        // Assert
        assertEquals("You do not own this task", exception.getMessage());
        verify(taskRepository).findById(taskId);
        verify(executionRepository, never()).existsByTaskAndDate(any(Task.class), any(LocalDate.class));
        verify(executionRepository, never()).save(any(ExecutionLog.class));
    }

    @Test
    void retrieveExecutionHistorySuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Task task = buildTask(taskId, userId);
        ExecutionLog firstLog = new ExecutionLog(task, LocalDate.of(2026, 4, 14), ExecutionStatus.DONE);
        ExecutionLog secondLog = new ExecutionLog(task, LocalDate.of(2026, 4, 15), ExecutionStatus.MISSED);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(executionRepository.findByTask(task)).thenReturn(List.of(firstLog, secondLog));

        // Act
        List<ExecutionResponseDTO> responses = executionService.getExecutionLogs(taskId, userId.toString());

        // Assert
        assertEquals(2, responses.size());
        assertEquals(LocalDate.of(2026, 4, 14), responses.get(0).getDate());
        assertEquals(ExecutionStatus.DONE, responses.get(0).getStatus());
        assertEquals(LocalDate.of(2026, 4, 15), responses.get(1).getDate());
        assertEquals(ExecutionStatus.MISSED, responses.get(1).getStatus());
        verify(taskRepository).findById(taskId);
        verify(executionRepository).findByTask(task);
    }

    private Task buildTask(UUID taskId, UUID userId) {
        User user = new User();
        user.setId(userId);
        user.setEmail("owner@example.com");

        Task task = new Task();
        task.setId(taskId);
        task.setTitle("Workout");
        task.setUser(user);
        return task;
    }
}
