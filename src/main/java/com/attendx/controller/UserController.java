package com.attendx.controller;

import com.attendx.dto.ApiResponse;
import com.attendx.dto.CreateUserRequest;
import com.attendx.dto.UserDto;
import com.attendx.model.User;
import com.attendx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    /**
     * GET /api/users
     * Used by ManageUsers.jsx to list all students and faculty
     * Admin can see all; Faculty can see students
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String className) {

        List<User> users;
        if (role != null && className != null) {
            users = userRepository.findByRoleAndClassName(role, className);
        } else if (role != null) {
            users = userRepository.findByRole(role);
        } else {
            // Return all non-admin users for ManageUsers.jsx
            users = userRepository.findAll().stream()
                .filter(u -> !u.getRole().equals("admin"))
                .toList();
        }

        List<UserDto> dtos = users.stream().map(UserDto::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    /**
     * GET /api/users/students
     * All 59 students — used by MarkAttendance.jsx, FacultyDashboard.jsx
     */
    @GetMapping("/students")
    public ResponseEntity<?> getStudents(
            @RequestParam(required = false) String className) {
        List<User> students = className != null
            ? userRepository.findByRoleAndClassName("student", className)
            : userRepository.findByRole("student");

        return ResponseEntity.ok(ApiResponse.ok(
            students.stream().map(UserDto::from).toList()
        ));
    }

    /**
     * GET /api/users/faculty
     * All faculty — used by TimetableManager.jsx
     */
    @GetMapping("/faculty")
    public ResponseEntity<?> getFaculty() {
        var faculty = userRepository.findByRole("faculty")
            .stream().map(UserDto::from).toList();
        return ResponseEntity.ok(ApiResponse.ok(faculty));
    }

    /**
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        return userRepository.findById(id)
            .map(u -> ResponseEntity.ok(ApiResponse.ok(UserDto.from(u))))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/users
     * ManageUsers.jsx "Add User" modal — Admin only
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email already in use"));
        }

        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(
            req.getPassword() != null ? req.getPassword() : "pass123"
        ));
        user.setRole(req.getRole());
        user.setDepartment(req.getDepartment());
        user.setRollNo(req.getRollNo());
        user.setClassName(req.getClassName());
        user.setAvatar(req.getAvatar() != null
            ? req.getAvatar()
            : req.getName().substring(0, Math.min(2, req.getName().length())).toUpperCase()
        );

        User saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("User created", UserDto.from(saved)));
    }

    /**
     * DELETE /api/users/{id}
     * ManageUsers.jsx delete button — Admin only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }

    /**
     * PUT /api/users/{id}
     * Update user details — Admin only
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable String id,
                                        @RequestBody CreateUserRequest req) {
        return userRepository.findById(id).map(user -> {
            if (req.getName()       != null) user.setName(req.getName());
            if (req.getDepartment() != null) user.setDepartment(req.getDepartment());
            if (req.getRollNo()     != null) user.setRollNo(req.getRollNo());
            if (req.getClassName()  != null) user.setClassName(req.getClassName());
            if (req.getPassword()   != null) user.setPassword(passwordEncoder.encode(req.getPassword()));
            return ResponseEntity.ok(ApiResponse.ok("User updated", UserDto.from(userRepository.save(user))));
        }).orElse(ResponseEntity.notFound().build());
    }
}
