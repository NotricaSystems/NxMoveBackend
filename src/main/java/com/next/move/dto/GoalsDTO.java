package com.next.move.dto;

import com.next.move.models.Goals;

import java.util.List;

public record GoalsDTO(
        Long id,
        String title,
        Integer estimatedDays,
        Integer estimatedHours,
        Boolean browserNotif,
        Boolean smsNotif,
        Boolean whatsappNotif,
        Integer intensity,
        Integer frequency,
        Integer status,
        Integer remainingSeconds,
        String startDate,
        String createdDate,
        String lastPausedDate,
        List<SubtasksDTO> subtasksList) {

    public static GoalsDTO fromEntity(Goals goal) {
        return new GoalsDTO(
                goal.getId(),
                goal.getTitle(),
                goal.getEstimatedDays(),
                goal.getEstimatedHours(),
                goal.getBrowserNotif(),
                goal.getSmsNotif(),
                goal.getWhatsappNotif(),
                goal.getIntensity(),
                goal.getFrequency(),
                goal.getStatus(),
                goal.getRemainingSeconds(),
                goal.getStartDate() == null ? "" : goal.getStartDate().toString(),
                goal.getCreatedDate() == null ? "" : goal.getCreatedDate().toString(),
                goal.getLastPausedDate() == null ? "" : goal.getLastPausedDate().toString(),
                goal.getSubtasksList() == null ? List.of() :
                        goal.getSubtasksList().stream()
                                .map(SubtasksDTO::fromEntity)
                                .toList()
        );
    }
}

