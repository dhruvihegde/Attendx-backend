package com.attendx.repository;

import com.attendx.model.QRSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface QRSessionRepository extends MongoRepository<QRSession, String> {
    Optional<QRSession> findBySessionIdAndStatus(String sessionId, String status);
    List<QRSession> findByFacultyIdOrderByStartedAtDesc(String facultyId);
    Optional<QRSession> findTopByStatusOrderByStartedAtDesc(String status);
}
