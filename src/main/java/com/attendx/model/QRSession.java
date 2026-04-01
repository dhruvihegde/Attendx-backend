package com.attendx.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "qr_sessions")
public class QRSession {

    @Id
    private String id;

    private String sessionId;  // e.g. "ATT-IOT-2025-03-10"

    private String subjectId;

    private String subjectName;

    private String className;  // "CE-ALL" or specific batch

    private String date;

    private String pin;        // 4-digit PIN

    private String facultyId;

    private long startedAt;    // epoch ms

    private Long endedAt;      // null if still active

    // "active" or "ended"
    private String status;
}
