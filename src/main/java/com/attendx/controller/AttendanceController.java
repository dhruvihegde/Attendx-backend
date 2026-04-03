package com.attendx.controller;

import com.attendx.dto.ApiResponse;
import com.attendx.dto.AttendanceMarkRequest;
import com.attendx.dto.SubjectStatsDto;
import com.attendx.model.AttendanceRecord;
import com.attendx.model.Notification;
import com.attendx.model.Subject;
import com.attendx.model.User;
import com.attendx.repository.AttendanceRecordRepository;
import com.attendx.repository.NotificationRepository;
import com.attendx.repository.SubjectRepository;
import com.attendx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired private AttendanceRecordRepository attendanceRepo;
    @Autowired private SubjectRepository subjectRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private NotificationRepository notifRepo;

    /**
     * POST /api/attendance/mark
     * MarkAttendance.jsx "Save Attendance" button
     * Faculty marks present/absent for all students in a subject for a given date
     */
    @PostMapping("/mark")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ResponseEntity<?> markAttendance(
            @RequestBody AttendanceMarkRequest req,
            @AuthenticationPrincipal User faculty) {

        List<AttendanceRecord> saved = new ArrayList<>();

        for (AttendanceMarkRequest.StudentStatus s : req.getRecords()) {
            // Upsert: update if exists, insert if not
            AttendanceRecord record = attendanceRepo
                    .findByStudentIdAndSubjectIdAndDate(s.getStudentId(), req.getSubjectId(), req.getDate())
                    .orElse(new AttendanceRecord());

            record.setStudentId(s.getStudentId());
            record.setSubjectId(req.getSubjectId());
            record.setDate(req.getDate());
            record.setStatus(s.getStatus());
            record.setMethod(req.getMethod() != null ? req.getMethod() : "manual");
            record.setMarkedBy(faculty.getId());

            saved.add(attendanceRepo.save(record));
        }

        // Auto-generate notifications for students below 75%
        Subject subject = subjectRepo.findById(req.getSubjectId()).orElse(null);
        String subjectName = subject != null ? subject.getName() : req.getSubjectId();

        for (AttendanceMarkRequest.StudentStatus s : req.getRecords()) {
            List<AttendanceRecord> allRecs = attendanceRepo.findByStudentId(s.getStudentId());
            int total   = allRecs.size();
            int present = (int) allRecs.stream().filter(r -> "present".equals(r.getStatus())).count();
            int pct     = total > 0 ? Math.round((float) present / total * 100) : 0;

            if (pct < 75 && total >= 3) {
                Notification notif = new Notification();
                notif.setUserId(s.getStudentId());
                notif.setType("warning");
                notif.setTitle("Low Attendance Alert");
                notif.setMessage("Your overall attendance is " + pct + "%. Minimum required is 75%.");
                notif.setTime("Just now");
                notif.setRead(false);
                notifRepo.save(notif);
            }
        }

        // Notify faculty that attendance was saved
        Notification facultyNotif = new Notification();
        facultyNotif.setUserId(faculty.getId());
        facultyNotif.setType("success");
        facultyNotif.setTitle("Attendance Saved");
        facultyNotif.setMessage("Attendance for " + subjectName + " on " + req.getDate() + " saved successfully.");
        facultyNotif.setTime("Just now");
        facultyNotif.setRead(false);
        notifRepo.save(facultyNotif);

        return ResponseEntity.ok(ApiResponse.ok(
                "Attendance saved for " + saved.size() + " students", saved.size()
        ));
    }

    /**
     * GET /api/attendance/student/{studentId}/stats
     * AttendanceView.jsx + StudentDashboard.jsx — subject-wise stats for a student
     */
    @GetMapping("/student/{studentId}/stats")
    public ResponseEntity<?> getStudentStats(@PathVariable String studentId) {
        List<Subject> subjects = subjectRepo.findAll();
        List<SubjectStatsDto> stats = subjects.stream().map(sub -> {
            // Total classes held for this subject = unique dates with any record
            int totalClasses = (int) attendanceRepo.findBySubjectId(sub.getId()).stream()
                    .map(AttendanceRecord::getDate).distinct().count();
            List<AttendanceRecord> recs = attendanceRepo
                    .findByStudentIdAndSubjectId(studentId, sub.getId());
            int present = (int) recs.stream().filter(r -> "present".equals(r.getStatus())).count();
            int absent  = totalClasses - present;
            int pct     = totalClasses > 0 ? Math.round((float) present / totalClasses * 100) : 0;
            return new SubjectStatsDto(sub.getId(), sub.getName(), totalClasses, present, absent, pct);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    /**
     * GET /api/attendance/student/{studentId}/records?subjectId=IOT
     * AttendanceView.jsx — daily records for a specific subject
     */
    @GetMapping("/student/{studentId}/records")
    public ResponseEntity<?> getStudentRecords(
            @PathVariable String studentId,
            @RequestParam(required = false) String subjectId) {

        List<AttendanceRecord> records = subjectId != null
                ? attendanceRepo.findByStudentIdAndSubjectIdOrderByDateDesc(studentId, subjectId)
                : attendanceRepo.findByStudentId(studentId);

        return ResponseEntity.ok(ApiResponse.ok(records));
    }

    /**
     * GET /api/attendance/subject/{subjectId}/history
     * AttendanceHistory.jsx — all student records for a subject (last 10 dates)
     */
    @GetMapping("/subject/{subjectId}/history")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ResponseEntity<?> getSubjectHistory(@PathVariable String subjectId) {
        List<AttendanceRecord> records = attendanceRepo.findBySubjectId(subjectId);
        return ResponseEntity.ok(ApiResponse.ok(records));
    }

    /**
     * GET /api/attendance/subject/{subjectId}/date/{date}
     * Get attendance for a specific subject on a specific date
     */
    @GetMapping("/subject/{subjectId}/date/{date}")
    @PreAuthorize("hasAnyRole('ADMIN','FACULTY')")
    public ResponseEntity<?> getBySubjectAndDate(
            @PathVariable String subjectId,
            @PathVariable String date) {
        return ResponseEntity.ok(ApiResponse.ok(
                attendanceRepo.findBySubjectIdAndDate(subjectId, date)
        ));
    }

    /**
     * GET /api/attendance/overall/{studentId}
     * Returns single overall attendance percentage for a student
     */
    @GetMapping("/overall/{studentId}")
    public ResponseEntity<?> getOverallAttendance(@PathVariable String studentId) {
        List<AttendanceRecord> recs = attendanceRepo.findByStudentId(studentId);
        int total   = recs.size();
        int present = (int) recs.stream().filter(r -> "present".equals(r.getStatus())).count();
        int pct     = total > 0 ? Math.round((float) present / total * 100) : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("studentId",  studentId);
        result.put("total",      total);
        result.put("present",    present);
        result.put("absent",     total - present);
        result.put("percentage", pct);
        result.put("isDefaulter", pct < 75);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * GET /api/attendance/analytics/summary
     * AdminDashboard.jsx + AnalyticsPage.jsx
     */
    @GetMapping("/analytics/summary")
    public ResponseEntity<?> getAnalyticsSummary() {
        List<User> students = userRepo.findByRole("student");
        List<User> faculty  = userRepo.findByRole("faculty");
        List<AttendanceRecord> allRecords = attendanceRepo.findAll();

        int total   = allRecords.size();
        int present = (int) allRecords.stream().filter(r -> "present".equals(r.getStatus())).count();
        int overall = total > 0 ? Math.round((float) present / total * 100) : 0;

        // Total classes held = unique dates across all records
        List<String> allDates = attendanceRepo.findAll().stream()
                .map(AttendanceRecord::getDate).distinct().collect(Collectors.toList());
        int totalClasses = allDates.size();

        long defaulters = students.stream().filter(s -> {
            List<AttendanceRecord> r = attendanceRepo.findByStudentId(s.getId());
            int p = (int) r.stream().filter(rec -> "present".equals(rec.getStatus())).count();
            // Students with no records have 0% — treat as defaulter if any classes held
            int pct = totalClasses > 0 ? Math.round((float) p / totalClasses * 100) : 0;
            return totalClasses > 0 && pct < 75;
        }).count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalStudents",     students.size());
        summary.put("totalFaculty",      faculty.size());
        summary.put("overallAttendance", overall);
        summary.put("defaulters",        defaulters);
        summary.put("eligibleStudents",  students.size() - defaulters);
        summary.put("hasData",           total > 0);

        return ResponseEntity.ok(ApiResponse.ok(summary));
    }


    /**
     * GET /api/attendance/analytics/subject-wise
     * AnalyticsPage.jsx subject-wise bar chart
     */
    @GetMapping("/analytics/subject-wise")
    public ResponseEntity<?> getSubjectWise() {
        List<Subject> subjects = subjectRepo.findAll();
        List<Map<String, Object>> result = subjects.stream().map(sub -> {
            List<AttendanceRecord> recs = attendanceRepo.findBySubjectId(sub.getId());
            int total   = recs.size();
            int present = (int) recs.stream().filter(r -> "present".equals(r.getStatus())).count();
            int absent  = total - present;
            int pct     = total > 0 ? Math.round((float) present / total * 100) : 0;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name",       sub.getId());
            m.put("fullName",   sub.getName());
            m.put("percentage", pct);
            m.put("present",    present);
            m.put("absent",     absent);
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * GET /api/attendance/analytics/student-wise
     * AnalyticsPage.jsx student-wise horizontal bar chart
     */
    @GetMapping("/analytics/student-wise")
    public ResponseEntity<?> getStudentWise() {
        List<User> students = userRepo.findByRole("student");
        // Total unique dates any class was held
        int totalClasses = (int) attendanceRepo.findAll().stream()
                .map(AttendanceRecord::getDate).distinct().count();

        List<Map<String, Object>> result = students.stream().map(s -> {
                    List<AttendanceRecord> recs = attendanceRepo.findByStudentId(s.getId());
                    int present = (int) recs.stream().filter(r -> "present".equals(r.getStatus())).count();
                    // Percentage out of total classes held, not just their own records
                    int pct = totalClasses > 0 ? Math.round((float) present / totalClasses * 100) : 0;
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",          s.getId());
                    m.put("name",        s.getName());
                    m.put("rollNo",      s.getRollNo());
                    m.put("className",   s.getClassName());
                    m.put("percentage",  pct);
                    m.put("isDefaulter", totalClasses > 0 && pct < 75);
                    return m;
                }).sorted((a, b) -> (int) b.get("percentage") - (int) a.get("percentage"))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * GET /api/attendance/analytics/batch-wise
     * AnalyticsPage.jsx batch-wise bar chart
     */
    @GetMapping("/analytics/batch-wise")
    public ResponseEntity<?> getBatchWise() {
        List<String> batches = List.of("CE-A1", "CE-A2", "CE-A3");
        List<Map<String, Object>> result = batches.stream().map(batch -> {
            List<User> batchStudents = userRepo.findByRoleAndClassName("student", batch);
            int totalPct = 0, defaulters = 0;
            int totalClasses = (int) attendanceRepo.findAll().stream()
                    .map(AttendanceRecord::getDate).distinct().count();
            for (User s : batchStudents) {
                List<AttendanceRecord> recs = attendanceRepo.findByStudentId(s.getId());
                int p = (int) recs.stream().filter(r -> "present".equals(r.getStatus())).count();
                int pct = totalClasses > 0 ? Math.round((float) p / totalClasses * 100) : 0;
                totalPct += pct;
                if (totalClasses > 0 && pct < 75) defaulters++;
            }
            int avg = batchStudents.isEmpty() ? 0 : totalPct / batchStudents.size();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("batch",          batch);
            m.put("students",       batchStudents.size());
            m.put("avgAttendance",  avg);
            m.put("defaulters",     defaulters);
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
