package com.attendx.dto;

import lombok.Data;

@Data
public class QRMarkRequest {
    private String sessionId;
    private String pin;
    private String subjectId;
}
