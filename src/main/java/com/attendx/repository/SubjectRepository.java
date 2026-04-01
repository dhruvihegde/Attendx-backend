package com.attendx.repository;

import com.attendx.model.Subject;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SubjectRepository extends MongoRepository<Subject, String> {
    List<Subject> findByFacultyId(String facultyId);
    List<Subject> findByClassName(String className);
}
