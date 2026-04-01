package com.attendx.controller;

import com.attendx.dto.ApiResponse;
import com.attendx.dto.TimetableSlotRequest;
import com.attendx.model.TimetableSlot;
import com.attendx.repository.TimetableSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/timetable")
public class TimetableController {

    @Autowired private TimetableSlotRepository timetableRepo;

    /** GET /api/timetable — all slots (Admin/TimetableManager.jsx) */
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(timetableRepo.findAll()));
    }

    /** GET /api/timetable/faculty/{facultyId} — FacultyTimetable.jsx */
    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<?> getByFaculty(@PathVariable String facultyId) {
        return ResponseEntity.ok(ApiResponse.ok(timetableRepo.findByFacultyId(facultyId)));
    }

    /** GET /api/timetable/class/{className} — StudentTimetable.jsx */
    @GetMapping("/class/{className}")
    public ResponseEntity<?> getByClass(@PathVariable String className) {
        return ResponseEntity.ok(ApiResponse.ok(timetableRepo.findByClassName(className)));
    }

    /** POST /api/timetable — Add slot (Admin only) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody TimetableSlotRequest req) {
        TimetableSlot slot = new TimetableSlot();
        slot.setDay(req.getDay());
        slot.setTime(req.getTime());
        slot.setSubject(req.getSubject());
        slot.setClassName(req.getClassName());
        slot.setRoom(req.getRoom());
        slot.setFacultyId(req.getFacultyId());
        return ResponseEntity.ok(ApiResponse.ok("Slot added", timetableRepo.save(slot)));
    }

    /** PUT /api/timetable/{id} — Edit slot (Admin + Faculty for their own) */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id,
                                    @RequestBody TimetableSlotRequest req) {
        return timetableRepo.findById(id).map(slot -> {
            if (req.getDay()       != null) slot.setDay(req.getDay());
            if (req.getTime()      != null) slot.setTime(req.getTime());
            if (req.getSubject()   != null) slot.setSubject(req.getSubject());
            if (req.getClassName() != null) slot.setClassName(req.getClassName());
            if (req.getRoom()      != null) slot.setRoom(req.getRoom());
            if (req.getFacultyId() != null) slot.setFacultyId(req.getFacultyId());
            return ResponseEntity.ok(ApiResponse.ok("Slot updated", timetableRepo.save(slot)));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/timetable/{id} — Admin only */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        timetableRepo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Slot deleted", null));
    }
}
