package com.next.move.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeEstimate {
    private Integer days;
    private Integer hours;
    private Integer frequency;
}


