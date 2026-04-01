package com.attendx.controller;

import com.attendx.dto.ApiResponse;
import com.attendx.dto.DepartmentRequest;
import com.attendx.model.Department;
import com.attendx.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired private DepartmentRepository deptRepo;

    /** GET /api/departments — ManageDepartments.jsx */
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(deptRepo.findAll()));
    }

    /** POST /api/departments — Admin only */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody DepartmentRequest req) {
        Department dept = new Department();
        dept.setName(req.getName());
        dept.setHod(req.getHod());
        dept.setStudents(req.getStudents());
        dept.setFaculty(req.getFaculty());
        return ResponseEntity.ok(ApiResponse.ok("Department created", deptRepo.save(dept)));
    }

    /** PUT /api/departments/{id} — Admin only */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable String id,
                                    @RequestBody DepartmentRequest req) {
        return deptRepo.findById(id).map(dept -> {
            if (req.getName()     != null) dept.setName(req.getName());
            if (req.getHod()      != null) dept.setHod(req.getHod());
            dept.setStudents(req.getStudents());
            dept.setFaculty(req.getFaculty());
            return ResponseEntity.ok(ApiResponse.ok("Updated", deptRepo.save(dept)));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/departments/{id} — Admin only */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        deptRepo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
