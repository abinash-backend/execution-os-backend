package com.executionos.task.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class StreakResponseDTO {

    private UUID taskId;
    private int currentStreak;
    private int longestStreak;
    private double consistencyScore;

    public StreakResponseDTO(UUID taskId, int currentStreak,
                             int longestStreak, double consistencyScore) {
        this.taskId = taskId;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.consistencyScore = consistencyScore;
    }

    public StreakResponseDTO() {}
}