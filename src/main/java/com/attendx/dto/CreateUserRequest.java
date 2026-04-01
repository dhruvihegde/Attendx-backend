package com.attendx.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String name;
    private String email;
    private String password;
    private String role;
    private String department;
    private String rollNo;
    private String className;
    private String avatar;
}
