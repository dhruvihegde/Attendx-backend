package com.attendx.dto;

import lombok.Data;

@Data
public class DepartmentRequest {
    private String name;
    private String hod;
    private int students;
    private int faculty;
}
