package com.next.move.dto;

import com.next.move.models.Subtasks;
public record SubtasksDTO(
        Long id,
        Integer priority,
        Integer percentage,
        String title,
        String description) {
    public static SubtasksDTO fromEntity(Subtasks subtask) {
        return new SubtasksDTO(
                subtask.getId(),
                subtask.getPriority(),
                subtask.getPercentage(),
                subtask.getTitle(),
                subtask.getDescription()
        );
    }
}

