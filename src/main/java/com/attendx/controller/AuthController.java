package com.attendx.controller;

import com.attendx.dto.ApiResponse;
import com.attendx.dto.LoginRequest;
import com.attendx.dto.LoginResponse;
import com.attendx.dto.UserDto;
import com.attendx.model.User;
import com.attendx.repository.UserRepository;
import com.attendx.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private PasswordEncoder passwordEncoder;

    /**
     * POST /api/auth/login
     * Matches AuthContext.jsx: login(email, password, role)
     * Returns { token, user } — frontend stores token in memory/localStorage
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        var userOpt = userRepository.findByEmail(req.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("Invalid credentials or role mismatch"));
        }

        User user = userOpt.get();

        // Role must match what user selected on login screen
        if (!user.getRole().equals(req.getRole())) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("Invalid credentials or role mismatch"));
        }

        // Check password (bcrypt)
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("Invalid credentials or role mismatch"));
        }

        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole());
        LoginResponse response = new LoginResponse(token, UserDto.from(user));

        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    /**
     * GET /api/auth/me
     * Returns current user info from JWT — useful for page refresh
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(@org.springframework.security.core.annotation.AuthenticationPrincipal Object principal) {
        if (principal instanceof User user) {
            return ResponseEntity.ok(ApiResponse.ok(UserDto.from(user)));
        }
        return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
    }

    /**
     * POST /api/auth/forgot-password
     * Matches ForgotPassword.jsx — sends reset link (stub: just returns success)
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        // In production: generate reset token, send email via JavaMailSender
        // For now, always return success to not expose whether email exists
        return ResponseEntity.ok(ApiResponse.ok("Reset link sent if email exists", null));
    }
}
