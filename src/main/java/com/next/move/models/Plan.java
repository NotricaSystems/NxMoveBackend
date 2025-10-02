package com.next.move.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan {
    private Subtasks[] subtasksList;
    private TimeEstimate estimatedTime;
}


