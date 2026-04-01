package com.attendx.dto;

import lombok.Data;

@Data
public class QRSessionStartRequest {
    private String sessionId;
    private String subjectId;
    private String subjectName;
    private String className;
    private String date;
}
