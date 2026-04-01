package com.attendx.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectStatsDto {
    private String subjectId;
    private String subjectName;
    private int total;
    private int present;
    private int absent;
    private int percentage;
}
