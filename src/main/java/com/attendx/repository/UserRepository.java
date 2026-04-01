package com.attendx.repository;

import com.attendx.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(String role);
    List<User> findByRoleAndClassName(String role, String className);
    boolean existsByEmail(String email);
}
