package com.attendx.controller;

import com.attendx.dto.ApiResponse;
import com.attendx.dto.QRMarkRequest;
import com.attendx.dto.QRSessionStartRequest;
import com.attendx.model.AttendanceRecord;
import com.attendx.model.QRSession;
import com.attendx.model.User;
import com.attendx.repository.AttendanceRecordRepository;
import com.attendx.repository.QRSessionRepository;
import com.attendx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/qr")
public class QRSessionController {

    @Autowired private QRSessionRepository sessionRepo;
    @Autowired private AttendanceRecordRepository attendanceRepo;
    @Autowired private UserRepository userRepo;

    /**
     * POST /api/qr/start
     * AttendanceCapture.jsx "Start Session + Generate PIN"
     * Faculty starts a session → backend generates PIN and saves session
     */
    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ResponseEntity<?> startSession(@RequestBody QRSessionStartRequest req,
                                          @AuthenticationPrincipal User faculty) {
        // End any existing active session for this faculty
        sessionRepo.findByFacultyIdOrderByStartedAtDesc(faculty.getId())
                .stream()
                .filter(s -> "active".equals(s.getStatus()))
                .forEach(s -> { s.setStatus("ended"); s.setEndedAt(System.currentTimeMillis()); sessionRepo.save(s); });

        // Generate 4-digit PIN
        String pin = String.format("%04d", (int)(Math.random() * 9000) + 1000);

        QRSession session = new QRSession();
        session.setSessionId(req.getSessionId());
        session.setSubjectId(req.getSubjectId());
        session.setSubjectName(req.getSubjectName());
        session.setClassName(req.getClassName() != null ? req.getClassName() : "CE-ALL");
        session.setDate(req.getDate());
        session.setPin(pin);
        session.setFacultyId(faculty.getId());
        session.setStartedAt(System.currentTimeMillis());
        session.setStatus("active");

        QRSession saved = sessionRepo.save(session);
        return ResponseEntity.ok(ApiResponse.ok("Session started", saved));
    }

    /**
     * POST /api/qr/end
     * AttendanceCapture.jsx "End Session Early" button
     */
    @PostMapping("/end")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ResponseEntity<?> endSession(@RequestBody java.util.Map<String, String> body,
                                        @AuthenticationPrincipal User faculty) {
        String sessionId = body.get("sessionId");
        Optional<QRSession> sessionOpt = sessionRepo.findBySessionIdAndStatus(sessionId, "active");
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("No active session found"));
        }
        QRSession session = sessionOpt.get();
        session.setStatus("ended");
        session.setEndedAt(System.currentTimeMillis());
        sessionRepo.save(session);
        return ResponseEntity.ok(ApiResponse.ok("Session ended", session));
    }

    /**
     * GET /api/qr/active
     * StudentDashboard.jsx polls this to check if a session is live
     * Returns active session (if any) — student uses this to show the PIN entry UI
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveSession() {
        Optional<QRSession> active = sessionRepo.findTopByStatusOrderByStartedAtDesc("active");
        return ResponseEntity.ok(ApiResponse.ok(active.orElse(null)));
    }

    /**
     * POST /api/qr/mark
     * StudentDashboard.jsx "Mark Present" button
     * Student verifies PIN and marks themselves present
     */
    @PostMapping("/mark")
    public ResponseEntity<?> markPresent(@RequestBody QRMarkRequest req,
                                         @AuthenticationPrincipal User student) {
        // Find the active session
        Optional<QRSession> sessionOpt = sessionRepo.findBySessionIdAndStatus(req.getSessionId(), "active");
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Session is not active or has expired"));
        }

        QRSession session = sessionOpt.get();

        // Verify PIN
        if (!session.getPin().equals(req.getPin())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Invalid PIN"));
        }

        // Check not already marked
        if (attendanceRepo.existsByStudentIdAndSubjectIdAndDate(
                student.getId(), session.getSubjectId(), session.getDate())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Already marked present"));
        }

        // Save attendance record
        AttendanceRecord record = new AttendanceRecord();
        record.setStudentId(student.getId());
        record.setSubjectId(session.getSubjectId());
        record.setDate(session.getDate());
        record.setStatus("present");
        record.setMethod("qr");
        record.setMarkedBy(session.getFacultyId());
        record.setSessionId(session.getSessionId());

        attendanceRepo.save(record);

        return ResponseEntity.ok(ApiResponse.ok("Attendance marked successfully", record));
    }

    /**
     * GET /api/qr/history/faculty/{facultyId}
     * AttendanceCapture.jsx session history panel
     */
    @GetMapping("/history/faculty/{facultyId}")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ResponseEntity<?> getSessionHistory(@PathVariable String facultyId) {
        List<QRSession> history = sessionRepo.findByFacultyIdOrderByStartedAtDesc(facultyId);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    /**
     * GET /api/qr/session/{sessionId}/attendance
     * Get list of students marked present in a QR session
     * Returns records enriched with student name, rollNo, avatar, className
     */
    @GetMapping("/session/{sessionId}/attendance")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ResponseEntity<?> getSessionAttendance(@PathVariable String sessionId) {
        List<AttendanceRecord> records = attendanceRepo.findAll().stream()
                .filter(r -> sessionId.equals(r.getSessionId()))
                .toList();

        // Enrich each record with student info
        List<java.util.Map<String, Object>> enriched = records.stream().map(r -> {
            java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("recordId",  r.getId());
            m.put("studentId", r.getStudentId());
            m.put("status",    r.getStatus());
            m.put("date",      r.getDate());
            m.put("time",      r.getSessionId());
            // Look up student details
            userRepo.findById(r.getStudentId()).ifPresent(u -> {
                m.put("name",      u.getName());
                m.put("rollNo",    u.getRollNo());
                m.put("avatar",    u.getAvatar());
                m.put("className", u.getClassName());
            });
            return m;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(enriched));
    }

    /**
     * POST /api/qr/expire
     * Called by frontend timer when 5 min runs out — ends session automatically
     */
    @PostMapping("/expire")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ResponseEntity<?> expireSession(@RequestBody java.util.Map<String, String> body,
                                           @AuthenticationPrincipal User faculty) {
        String sessionId = body.get("sessionId");
        Optional<QRSession> sessionOpt = sessionRepo.findBySessionIdAndStatus(sessionId, "active");
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok("Session already ended", null));
        }
        QRSession session = sessionOpt.get();
        session.setStatus("ended");
        session.setEndedAt(System.currentTimeMillis());
        sessionRepo.save(session);
        return ResponseEntity.ok(ApiResponse.ok("Session expired", session));
    }
}
