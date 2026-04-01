package com.attendx.repository;

import com.attendx.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdOrderByReadAscIdDesc(String userId);
    long countByUserIdAndReadFalse(String userId);
}
