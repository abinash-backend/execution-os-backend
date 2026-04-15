package com.executionos.auth.controller;

import com.executionos.auth.entity.User;
import com.executionos.auth.repository.UserRepository;
import com.executionos.common.config.SecurityConfig;
import com.executionos.common.security.CustomUserDetailsService;
import com.executionos.common.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void registerUserSuccess() throws Exception {
        // Arrange
        User request = new User();
        request.setEmail("new.user@example.com");
        request.setPassword("plain-password");

        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // Assert
                .andExpect(status().isOk())
                .andExpect(content().string("User registered"));

        verify(passwordEncoder).encode("plain-password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loginSuccess() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        User request = new User();
        request.setEmail("existing.user@example.com");
        request.setPassword("plain-password");

        User storedUser = new User();
        storedUser.setId(userId);
        storedUser.setEmail("existing.user@example.com");
        storedUser.setPassword("encoded-password");

        when(userRepository.findByEmail("existing.user@example.com")).thenReturn(Optional.of(storedUser));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken(userId.toString())).thenReturn("jwt-token");

        // Act
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // Assert
                .andExpect(status().isOk())
                .andExpect(content().string("jwt-token"));
    }

    @Test
    void invalidLoginRequestValidation() throws Exception {
        // Arrange
        String invalidJson = "{\"email\":\"user@example.com\",\"password\":";

        // Act
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))

                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Invalid request payload"))
                .andExpect(jsonPath("$.method").value("POST"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
    }
}
