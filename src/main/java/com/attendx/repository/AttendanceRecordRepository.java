package com.attendx.repository;

import com.attendx.model.AttendanceRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends MongoRepository<AttendanceRecord, String> {
    List<AttendanceRecord> findByStudentId(String studentId);
    List<AttendanceRecord> findBySubjectId(String subjectId);
    List<AttendanceRecord> findByStudentIdAndSubjectId(String studentId, String subjectId);
    List<AttendanceRecord> findBySubjectIdAndDate(String subjectId, String date);
    List<AttendanceRecord> findByStudentIdAndSubjectIdOrderByDateDesc(String studentId, String subjectId);
    Optional<AttendanceRecord> findByStudentIdAndSubjectIdAndDate(String studentId, String subjectId, String date);
    boolean existsByStudentIdAndSubjectIdAndDate(String studentId, String subjectId, String date);
}
