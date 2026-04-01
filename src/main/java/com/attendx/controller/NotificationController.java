package com.attendx.controller;

import com.attendx.dto.ApiResponse;
import com.attendx.model.Notification;
import com.attendx.model.User;
import com.attendx.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private NotificationRepository notifRepo;

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
}
