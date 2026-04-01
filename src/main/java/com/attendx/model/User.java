package com.attendx.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    private String password;

    // "admin", "faculty", "student"
    private String role;

    private String department;

    private String avatar;

    // Faculty / Student specific
    private String rollNo;     // e.g. 24CE1001 (students only)
    private String className;  // e.g. CE-A1, CE-A2, CE-A3 (students only)

    private boolean isDefaulter = false;
}
