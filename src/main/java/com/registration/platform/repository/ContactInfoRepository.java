package com.registration.platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.registration.platform.model.entity.ContactInfo;

@Repository
public interface ContactInfoRepository extends JpaRepository<ContactInfo, Long> {
    // Basic CRUD methods are inherited.
    // findById(userId) will retrieve the contact info for a specific user
    // due to the shared primary key (@MapsId).
}