package com.attendx.dto;

import lombok.Data;

@Data
public class TimetableSlotRequest {
    private String day;
    private String time;
    private String subject;
    private String className;
    private String room;
    private String facultyId;
}
