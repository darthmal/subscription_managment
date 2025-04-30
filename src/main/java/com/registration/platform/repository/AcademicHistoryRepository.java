package com.registration.platform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.registration.platform.model.entity.AcademicHistory;
import com.registration.platform.model.entity.User;

@Repository
public interface AcademicHistoryRepository extends JpaRepository<AcademicHistory, Long> {

    // Find all academic history entries for a specific user
    // Ordering can be specified here or rely on the @OrderBy in the User entity
    List<AcademicHistory> findByUserOrderByStartDateDesc(User user);

    // Find by ID and User (useful for checking ownership before update/delete)
    // Optional<AcademicHistory> findByIdAndUser(Long id, User user);
    // JpaRepository already provides findById, ownership check should be done in service layer

}