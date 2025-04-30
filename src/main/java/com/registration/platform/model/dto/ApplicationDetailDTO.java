package com.registration.platform.model.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.registration.platform.model.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDetailDTO {

    private UserSummaryDTO userSummary; // Basic user info
    private PersonalInfoDTO personalInfo;
    private ContactInfoDTO contactInfo;
    private List<AcademicHistoryDTO> academicHistory;
    private List<DocumentDTO> documents;

    // Factory method to build from a User entity (assuming related entities are fetched)
    public static ApplicationDetailDTO fromUser(User user) {
        if (user == null) {
            return null;
        }

        // Manual mapping (Consider MapStruct for complex mappings)
        PersonalInfoDTO personalInfoDTO = (user.getPersonalInfo() != null)
            ? PersonalInfoDTO.builder()
                .lastName(user.getPersonalInfo().getLastName())
                .firstNames(user.getPersonalInfo().getFirstNames())
                .gender(user.getPersonalInfo().getGender())
                .dateOfBirth(user.getPersonalInfo().getDateOfBirth())
                .nationality(user.getPersonalInfo().getNationality())
                .idDocumentType(user.getPersonalInfo().getIdDocumentType())
                .build()
            : null;

        ContactInfoDTO contactInfoDTO = (user.getContactInfo() != null)
            ? ContactInfoDTO.builder()
                .phoneNumber(user.getContactInfo().getPhoneNumber())
                .emailVerified(user.getContactInfo().isEmailVerified())
                .address(user.getContactInfo().getAddress() != null ? AddressDTO.builder()
                    .street(user.getContactInfo().getAddress().getStreet())
                    .street2(user.getContactInfo().getAddress().getStreet2())
                    .city(user.getContactInfo().getAddress().getCity())
                    .postalCode(user.getContactInfo().getAddress().getPostalCode())
                    .country(user.getContactInfo().getAddress().getCountry())
                    .latitude(user.getContactInfo().getAddress().getLatitude())
                    .longitude(user.getContactInfo().getAddress().getLongitude())
                    .build() : null)
                .emergencyContact(user.getContactInfo().getEmergencyContact() != null ? EmergencyContactDTO.builder()
                    .name(user.getContactInfo().getEmergencyContact().getName())
                    .relationship(user.getContactInfo().getEmergencyContact().getRelationship())
                    .phone(user.getContactInfo().getEmergencyContact().getPhone())
                    .build() : null)
                .build()
            : null;

        List<AcademicHistoryDTO> academicHistoryDTOs = user.getAcademicHistories().stream()
            .map(h -> AcademicHistoryDTO.builder()
                .id(h.getId())
                .institutionName(h.getInstitutionName())
                .specialization(h.getSpecialization())
                .startDate(h.getStartDate())
                .endDate(h.getEndDate())
                .build())
            .collect(Collectors.toList());

        // Assuming documents need to be fetched separately or are eagerly loaded (adjust fetch strategy if needed)
        // For now, let's assume they are fetched when needed by the service. We'll pass null here.
        // List<DocumentDTO> documentDTOs = ... fetch and map documents ...

        return ApplicationDetailDTO.builder()
                .userSummary(UserSummaryDTO.fromUser(user))
                .personalInfo(personalInfoDTO)
                .contactInfo(contactInfoDTO)
                .academicHistory(academicHistoryDTOs)
                .documents(null) // Documents will be populated by the service method
                .build();
    }
}