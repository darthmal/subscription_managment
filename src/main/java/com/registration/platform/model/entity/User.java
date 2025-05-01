package com.registration.platform.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList; // Import ArrayList
import java.util.Collection;
import java.util.HashSet;
import java.util.List; // Import List
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany; // Import OneToMany
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy; // Import OrderBy
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user", // Using "_user" because "user" might be a reserved keyword in some DBs
       indexes = {
           @Index(name = "idx_user_email", columnList = "email", unique = true)
       })
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = true) // Password might be null for OAuth users initially
    private String password;

    // Basic user info - might be expanded or moved to a profile entity later
    @Column(length = 50)
    private String firstName;

    @Column(length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER) // Eager fetch roles for security checks
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default // Initialize with default value
    private Set<Role> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AuthProvider provider; // To track if registered via EMAIL, GOOGLE, MICROSOFT

    private String providerId; // External provider's user ID

    @Column(nullable = false)
    @Builder.Default
    private Boolean locked = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true; // Users are enabled by default, might require email verification later

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    // Add columnDefinition to set a DB-level default for existing rows
    @Column(name = "application_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'PENDING'")
    @Builder.Default
    private ApplicationStatus applicationStatus = ApplicationStatus.PENDING; // Default status for new Java objects

    // --- Relationships ---

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private PersonalInfo personalInfo;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startDate DESC") // Order by start date, newest first
    @Builder.Default // Initialize with default value
    private List<AcademicHistory> academicHistories = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ContactInfo contactInfo;

    // --- Lifecycle Callbacks ---

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- UserDetails Implementation ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        // Using email as the username for authentication
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Or add logic for account expiration
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Or add logic for password expiration
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // --- Helper methods for bidirectional relationship ---

    public void setPersonalInfo(PersonalInfo personalInfo) {
        if (personalInfo == null) {
            if (this.personalInfo != null) {
                this.personalInfo.setUser(null);
            }
        } else {
            personalInfo.setUser(this);
        }
        this.personalInfo = personalInfo;
    }

    public void addAcademicHistory(AcademicHistory history) {
        academicHistories.add(history);
        history.setUser(this);
    }

    public void removeAcademicHistory(AcademicHistory history) {
        academicHistories.remove(history);
        history.setUser(null);
    }

    public void setContactInfo(ContactInfo contactInfo) {
        if (contactInfo == null) {
            if (this.contactInfo != null) {
                this.contactInfo.setUser(null);
            }
        } else {
            contactInfo.setUser(this);
        }
        this.contactInfo = contactInfo;
    }
}