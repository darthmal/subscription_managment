package com.registration.platform.service.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.registration.platform.model.dto.AddressDTO;
import com.registration.platform.model.dto.ContactInfoDTO;
import com.registration.platform.model.dto.EmergencyContactDTO;
import com.registration.platform.model.entity.Address;
import com.registration.platform.model.entity.ContactInfo;
import com.registration.platform.model.entity.EmergencyContact;
import com.registration.platform.model.entity.User;
import com.registration.platform.repository.UserRepository;
import com.registration.platform.service.ContactInfoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContactInfoServiceImpl implements ContactInfoService {

    private static final Logger log = LoggerFactory.getLogger(ContactInfoServiceImpl.class);

    private final UserRepository userRepository;
    // private final ContactInfoRepository contactInfoRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<ContactInfoDTO> getContactInfo(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        return Optional.ofNullable(currentUser.getContactInfo())
                .map(this::mapEntityToDto); // Map entity to DTO if present
    }

    @Override
    @Transactional
    public ContactInfo saveOrUpdateContactInfo(ContactInfoDTO contactInfoDTO, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        log.info("Saving/updating contact info for user ID: {}", currentUser.getId());

        ContactInfo contactInfo = currentUser.getContactInfo();

        if (contactInfo == null) {
            log.debug("No existing contact info found for user ID: {}. Creating new.", currentUser.getId());
            contactInfo = new ContactInfo();
            // Ensure embedded objects are initialized for mapping
            contactInfo.setAddress(new Address());
            contactInfo.setEmergencyContact(new EmergencyContact());
            // Email verification status defaults to false in the entity
        } else {
            log.debug("Existing contact info found for user ID: {}. Updating.", currentUser.getId());
            // Ensure embedded objects exist before mapping (shouldn't be null if persisted)
             if (contactInfo.getAddress() == null) contactInfo.setAddress(new Address());
             if (contactInfo.getEmergencyContact() == null) contactInfo.setEmergencyContact(new EmergencyContact());
        }

        // Map DTO fields to entity (excluding emailVerified)
        mapDtoToEntity(contactInfoDTO, contactInfo);

        // Set the relationship on the User side to ensure consistency and trigger cascade
        currentUser.setContactInfo(contactInfo);

        // Save the User entity, which will cascade the save/update to ContactInfo
        userRepository.save(currentUser);
        log.info("Successfully saved/updated contact info for user ID: {}", currentUser.getId());

        // Return the managed ContactInfo entity associated with the user
        return currentUser.getContactInfo();
    }

    // --- Helper Methods ---

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User must be authenticated to perform this operation.");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    // Manual mapping methods (Consider MapStruct)
    private ContactInfoDTO mapEntityToDto(ContactInfo entity) {
        return ContactInfoDTO.builder()
                .phoneNumber(entity.getPhoneNumber())
                .emailVerified(entity.isEmailVerified())
                .address(mapAddressEntityToDto(entity.getAddress()))
                .emergencyContact(mapEmergencyContactEntityToDto(entity.getEmergencyContact()))
                .build();
    }

    private AddressDTO mapAddressEntityToDto(Address entity) {
        if (entity == null) return null;
        return AddressDTO.builder()
                .street(entity.getStreet())
                .street2(entity.getStreet2())
                .city(entity.getCity())
                .postalCode(entity.getPostalCode())
                .country(entity.getCountry())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();
    }

    private EmergencyContactDTO mapEmergencyContactEntityToDto(EmergencyContact entity) {
         if (entity == null) return null;
        return EmergencyContactDTO.builder()
                .name(entity.getName())
                .relationship(entity.getRelationship())
                .phone(entity.getPhone())
                .build();
    }

    private void mapDtoToEntity(ContactInfoDTO dto, ContactInfo entity) {
        entity.setPhoneNumber(dto.getPhoneNumber());
        // Do not map emailVerified from DTO - managed internally
        mapAddressDtoToEntity(dto.getAddress(), entity.getAddress());
        mapEmergencyContactDtoToEntity(dto.getEmergencyContact(), entity.getEmergencyContact());
        // Timestamps are handled by @PrePersist/@PreUpdate
    }

     private void mapAddressDtoToEntity(AddressDTO dto, Address entity) {
         if (dto == null || entity == null) return;
         entity.setStreet(dto.getStreet());
         entity.setStreet2(dto.getStreet2());
         entity.setCity(dto.getCity());
         entity.setPostalCode(dto.getPostalCode());
         entity.setCountry(dto.getCountry());
         // Geolocation fields might be set by a separate process
         // entity.setLatitude(dto.getLatitude());
         // entity.setLongitude(dto.getLongitude());
     }

     private void mapEmergencyContactDtoToEntity(EmergencyContactDTO dto, EmergencyContact entity) {
         if (dto == null || entity == null) return;
         entity.setName(dto.getName());
         entity.setRelationship(dto.getRelationship());
         entity.setPhone(dto.getPhone());
     }
}