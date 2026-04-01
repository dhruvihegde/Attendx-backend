package com.attendx.dto;

import com.attendx.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String name;
    private String email;
    private String role;
    private String department;
    private String avatar;
    private String rollNo;
    private String className;
    private boolean isDefaulter;

    public static UserDto from(User u) {
        UserDto dto = new UserDto();
        dto.setId(u.getId());
        dto.setName(u.getName());
        dto.setEmail(u.getEmail());
        dto.setRole(u.getRole());
        dto.setDepartment(u.getDepartment());
        dto.setAvatar(u.getAvatar());
        dto.setRollNo(u.getRollNo());
        dto.setClassName(u.getClassName());
        dto.setDefaulter(u.isDefaulter());
        return dto;
    }
}
