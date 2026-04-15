package com.executionos.task.controller;

import com.executionos.common.config.SecurityConfig;
import com.executionos.common.security.CustomUserDetailsService;
import com.executionos.common.security.JwtUtil;
import com.executionos.common.util.Frequency;
import com.executionos.common.util.Priority;
import com.executionos.common.util.Status;
import com.executionos.task.dto.TaskResponseDTO;
import com.executionos.task.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void createTaskSuccessfully() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        TaskResponseDTO response = buildTaskResponse(taskId, "Daily Planning", Priority.HIGH, Status.PENDING);
        when(taskService.createTask(org.mockito.ArgumentMatchers.any(), eq(UUID.fromString("11111111-1111-1111-1111-111111111111"))))
                .thenReturn(response);

        String requestBody = """
                {
                  "title": "Daily Planning",
                  "description": "Review top priorities",
                  "frequency": "DAILY",
                  "deadline": "2026-04-30",
                  "priority": "HIGH"
                }
                """;

        // Act
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))

                // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Daily Planning"))
                .andExpect(jsonPath("$.description").value("Sample description"))
                .andExpect(jsonPath("$.frequency").value("DAILY"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(taskService).createTask(org.mockito.ArgumentMatchers.any(), eq(UUID.fromString("11111111-1111-1111-1111-111111111111")));
    }

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void retrieveTasks() throws Exception {
        // Arrange
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        TaskResponseDTO firstTask = buildTaskResponse(UUID.randomUUID(), "Read", Priority.MEDIUM, Status.PENDING);
        TaskResponseDTO secondTask = buildTaskResponse(UUID.randomUUID(), "Code", Priority.HIGH, Status.DONE);

        when(taskService.getTasksByUser(userId, null, null)).thenReturn(List.of(firstTask, secondTask));

        // Act
        mockMvc.perform(get("/api/v1/tasks"))

                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Read"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].title").value("Code"))
                .andExpect(jsonPath("$[1].status").value("DONE"));
    }

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void filterTasksByStatus() throws Exception {
        // Arrange
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        TaskResponseDTO filteredTask = buildTaskResponse(UUID.randomUUID(), "Workout", Priority.HIGH, Status.DONE);

        when(taskService.getTasksByUser(userId, Status.DONE, null)).thenReturn(List.of(filteredTask));

        // Act
        mockMvc.perform(get("/api/v1/tasks")
                        .param("status", "DONE"))

                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Workout"))
                .andExpect(jsonPath("$[0].status").value("DONE"))
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void validationFailureOnInvalidRequest() throws Exception {
        // Arrange
        String invalidRequestBody = """
                {
                  "title": "",
                  "frequency": null
                }
                """;

        // Act
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))

                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.method").value("POST"))
                .andExpect(jsonPath("$.path").value("/api/v1/tasks"));
    }

    @Test
    void protectedEndpointsRequireAuthentication() throws Exception {
        // Arrange

        // Act
        mockMvc.perform(get("/api/v1/tasks"))

                // Assert
                .andExpect(status().isForbidden());
    }

    private TaskResponseDTO buildTaskResponse(UUID taskId, String title, Priority priority, Status status) {
        TaskResponseDTO response = new TaskResponseDTO();
        response.setId(taskId);
        response.setTitle(title);
        response.setDescription("Sample description");
        response.setFrequency(Frequency.DAILY);
        response.setDeadline(LocalDate.of(2026, 4, 30));
        response.setPriority(priority);
        response.setStatus(status);
        response.setCreatedAt(LocalDateTime.of(2026, 4, 15, 12, 0));
        return response;
    }
}
