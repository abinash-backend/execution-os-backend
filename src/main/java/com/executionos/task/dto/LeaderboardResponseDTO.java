package com.executionos.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardResponseDTO {
    private UUID userId;
    private double consistencyScore;
}