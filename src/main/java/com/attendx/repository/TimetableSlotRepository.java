package com.attendx.repository;

import com.attendx.model.TimetableSlot;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TimetableSlotRepository extends MongoRepository<TimetableSlot, String> {
    List<TimetableSlot> findByFacultyId(String facultyId);
    List<TimetableSlot> findByClassName(String className);
    List<TimetableSlot> findByDay(String day);
}
