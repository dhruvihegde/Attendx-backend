package com.attendx.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "subjects")
public class Subject {

    @Id
    private String id;   // e.g. "IOT", "DSE"

    private String name; // e.g. "Internet of Things (IOT)"

    private String department;

    // "CE-ALL" means all students attend
    private String className;

    private String facultyId; // references User.id
}
