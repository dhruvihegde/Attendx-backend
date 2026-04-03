package com.attendx.controller;

import com.attendx.dto.ApiResponse;
import com.attendx.model.Notification;
import com.attendx.model.User;
import com.attendx.repository.NotificationRepository;
import com.attendx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private NotificationRepository notifRepo;
    @Autowired private UserRepository userRepo;

    /**
     * GET /api/notifications
     * NotificationsPage.jsx — get all notifications for logged-in user
     */
    @GetMapping
    public ResponseEntity<?> getMyNotifications(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(
                notifRepo.findByUserIdOrderByReadAscIdDesc(user.getId())
        ));
    }

    /**
     * GET /api/notifications/unread-count
     * Navbar.jsx bell badge count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@AuthenticationPrincipal User user) {
        long count = notifRepo.countByUserIdAndReadFalse(user.getId());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", count)));
    }

    /**
     * PATCH /api/notifications/{id}/read
     * NotificationsPage.jsx — mark single notification as read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markRead(@PathVariable String id,
                                      @AuthenticationPrincipal User user) {
        return notifRepo.findById(id).map(n -> {
            if (!n.getUserId().equals(user.getId())) {
                return ResponseEntity.status(403).<Object>body(ApiResponse.error("Forbidden"));
            }
            n.setRead(true);
            return ResponseEntity.ok((Object) ApiResponse.ok("Marked as read", notifRepo.save(n)));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * PATCH /api/notifications/mark-all-read
     * NotificationsPage.jsx "Mark all read" button
     */
    @PatchMapping("/mark-all-read")
    public ResponseEntity<?> markAllRead(@AuthenticationPrincipal User user) {
        var notifs = notifRepo.findByUserIdOrderByReadAscIdDesc(user.getId());
        notifs.forEach(n -> n.setRead(true));
        notifRepo.saveAll(notifs);
        return ResponseEntity.ok(ApiResponse.ok("All marked as read", null));
    }

    /**
     * POST /api/notifications
     * Admin can push notifications to users
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Notification notif) {
        return ResponseEntity.ok(ApiResponse.ok("Notification sent", notifRepo.save(notif)));
    }

    /**
     * POST /api/notifications/seed
     * One-time endpoint to seed notifications into existing DB
     * Call this once after deployment to populate notifications
     */
    @PostMapping("/seed")
    public ResponseEntity<?> seedNotifications() {
        // Clear existing notifications first
        notifRepo.deleteAll();

        List<Notification> notifs = new ArrayList<>();

        // Admin notifications
        notifs.add(n("a1","warning","Defaulter Alert",
                "Several students are below 75% attendance. Review the analytics page.","Today"));
        notifs.add(n("a1","success","System Ready",
                "AttendX is live! All 59 students and 7 faculty have been loaded.","Today"));
        notifs.add(n("a1","info","Semester IV Active",
                "Timetable published for CE-A1, CE-A2, CE-A3 batches.","Yesterday"));

        // Faculty notifications
        for (String fId : List.of("f1","f2","f3","f4","f5","f6","f7")) {
            notifs.add(n(fId,"info","Mark Attendance",
                    "Don't forget to mark attendance for today's classes.","Today"));
            notifs.add(n(fId,"warning","Low Attendance Students",
                    "Several students in your subject are below 75%. Check History page.","Yesterday"));
            notifs.add(n(fId,"success","QR Session Ready",
                    "You can start a QR session anytime from the QR Attendance page.","2 days ago"));
        }

        // Defaulter student notifications
        for (String sId : List.of("s12","s19","s25","s34","s44")) {
            notifs.add(n(sId,"warning","Low Attendance Alert",
                    "Your attendance is below 75%. You may be barred from exams. Please attend regularly.","Today"));
        }

        // General notifications for all 59 students
        List<User> students = userRepo.findByRole("student");
        for (User s : students) {
            notifs.add(n(s.getId(),"info","Mid-Semester Exams",
                    "Mid-semester exams are scheduled from next Monday. Check your timetable.","2 days ago"));
            notifs.add(n(s.getId(),"info","Holiday Notice",
                    "College holiday on account of Holi — 14th March 2026.","3 days ago"));
        }

        notifRepo.saveAll(notifs);
        return ResponseEntity.ok(ApiResponse.ok("Seeded " + notifs.size() + " notifications", notifs.size()));
    }

    private Notification n(String userId, String type, String title, String message, String time) {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setType(type);
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setTime(time);
        notif.setRead(false);
        return notif;
    }
}
