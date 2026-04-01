package com.attendx.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "attendance_records")
@CompoundIndexes({
    @CompoundIndex(name = "student_subject_date", def = "{'studentId': 1, 'subjectId': 1, 'date': 1}", unique = true)
})
public class AttendanceRecord {

    @Id
    private String id;

    private String studentId;  // references User.id

    private String subjectId;  // references Subject.id

    // ISO date string: "2025-03-10"
    private String date;

    // "present" or "absent"
    private String status;

    // "manual", "qr"
    private String method;

    // Faculty who marked this record
    private String markedBy;

    // Optional: session id for QR-based attendance
    private String sessionId;
}
