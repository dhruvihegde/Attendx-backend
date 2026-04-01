package com.attendx.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "timetable")
public class TimetableSlot {

    @Id
    private String id;

    // "Monday", "Tuesday", etc.
    private String day;

    // e.g. "08:00-09:00"
    private String time;

    // Subject id e.g. "IOT"
    private String subject;

    // "CE-ALL" or specific class
    private String className;

    private String room;

    // Faculty User id
    private String facultyId;
}
