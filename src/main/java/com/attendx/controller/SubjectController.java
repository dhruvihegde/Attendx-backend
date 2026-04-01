package com.attendx.controller;

import com.attendx.dto.ApiResponse;
import com.attendx.model.Subject;
import com.attendx.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired private SubjectRepository subjectRepo;

    /** GET /api/subjects — all subjects */
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(subjectRepo.findAll()));
    }

    /** GET /api/subjects/faculty/{facultyId} — subjects assigned to a faculty */
    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<?> getByFaculty(@PathVariable String facultyId) {
        return ResponseEntity.ok(ApiResponse.ok(subjectRepo.findByFacultyId(facultyId)));
    }

    /** POST /api/subjects — Admin only */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody Subject subject) {
        return ResponseEntity.ok(ApiResponse.ok("Subject created", subjectRepo.save(subject)));
    }

    /** DELETE /api/subjects/{id} — Admin only */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        subjectRepo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
