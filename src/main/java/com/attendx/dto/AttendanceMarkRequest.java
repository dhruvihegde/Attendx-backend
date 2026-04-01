package com.attendx.dto;

import lombok.Data;
import java.util.List;

@Data
public class AttendanceMarkRequest {
    private String subjectId;
    private String date;
    // "manual" or "qr"
    private String method;
    private List<StudentStatus> records;

    @Data
    public static class StudentStatus {
        private String studentId;
        // "present" or "absent"
        private String status;
    }
}
