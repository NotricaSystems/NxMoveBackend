package com.next.move.repository;

import com.next.move.models.UserProfile;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserProfile, Long> {
    // Custom finder methods
    Optional<UserProfile> findByEmail(String email);
    UserProfile findByPhone(String phone);
}
