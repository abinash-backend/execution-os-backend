package com.executionos.execution.controller;

import com.executionos.common.config.SecurityConfig;
import com.executionos.common.exception.DuplicateResourceException;
import com.executionos.common.security.CustomUserDetailsService;
import com.executionos.common.security.JwtUtil;
import com.executionos.common.util.ExecutionStatus;
import com.executionos.execution.dto.ExecutionResponseDTO;
import com.executionos.execution.service.ExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExecutionController.class)
@Import(SecurityConfig.class)
class ExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExecutionService executionService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void logExecutionSuccessfully() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        ExecutionResponseDTO response = new ExecutionResponseDTO();
        response.setDate(LocalDate.of(2026, 4, 15));
        response.setStatus(ExecutionStatus.DONE);

        when(executionService.markExecution(eq(taskId), any(), eq("11111111-1111-1111-1111-111111111111")))
                .thenReturn(response);

        String requestBody = """
                {
                  "status": "DONE"
                }
                """;

        // Act
        mockMvc.perform(post("/api/v1/tasks/{taskId}/execution", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))

                // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date").value("2026-04-15"))
                .andExpect(jsonPath("$.status").value("DONE"));

        verify(executionService).markExecution(eq(taskId), any(), eq("11111111-1111-1111-1111-111111111111"));
    }

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void preventDuplicateExecutionLogs() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        when(executionService.markExecution(eq(taskId), any(), eq("11111111-1111-1111-1111-111111111111")))
                .thenThrow(new DuplicateResourceException("Execution already logged for today"));

        String requestBody = """
                {
                  "status": "DONE"
                }
                """;

        // Act
        mockMvc.perform(post("/api/v1/tasks/{taskId}/execution", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))

                // Assert
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Execution already logged for today"))
                .andExpect(jsonPath("$.path").value("/api/v1/tasks/" + taskId + "/execution"));
    }

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void retrieveExecutionHistory() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();
        ExecutionResponseDTO firstLog = new ExecutionResponseDTO();
        firstLog.setDate(LocalDate.of(2026, 4, 14));
        firstLog.setStatus(ExecutionStatus.DONE);

        ExecutionResponseDTO secondLog = new ExecutionResponseDTO();
        secondLog.setDate(LocalDate.of(2026, 4, 15));
        secondLog.setStatus(ExecutionStatus.MISSED);

        when(executionService.getExecutionLogs(taskId, "11111111-1111-1111-1111-111111111111"))
                .thenReturn(List.of(firstLog, secondLog));

        // Act
        mockMvc.perform(get("/api/v1/tasks/{taskId}/execution", taskId))

                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-04-14"))
                .andExpect(jsonPath("$[0].status").value("DONE"))
                .andExpect(jsonPath("$[1].date").value("2026-04-15"))
                .andExpect(jsonPath("$[1].status").value("MISSED"));
    }

    @Test
    void protectedExecutionEndpointsRequireAuthentication() throws Exception {
        // Arrange
        UUID taskId = UUID.randomUUID();

        // Act
        mockMvc.perform(get("/api/v1/tasks/{taskId}/execution", taskId))

                // Assert
                .andExpect(status().isForbidden());
    }
}
