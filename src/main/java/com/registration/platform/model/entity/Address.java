package com.registration.platform.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable // Marks this class as embeddable
public class Address {

    @NotBlank(message = "Street address cannot be blank")
    @Size(max = 255)
    @Column(name = "address_street") // Prefix column names to avoid conflicts
    private String street;

    @Size(max = 100)
    @Column(name = "address_street2")
    private String street2; // Optional second line

    @NotBlank(message = "City cannot be blank")
    @Size(max = 100)
    @Column(name = "address_city")
    private String city;

    @NotBlank(message = "Postal code cannot be blank")
    @Size(max = 20)
    @Column(name = "address_postal_code")
    private String postalCode;

    @NotBlank(message = "Country cannot be blank")
    @Size(max = 100)
    @Column(name = "address_country")
    private String country;

    // Optional fields for geolocation API results
    @Column(name = "address_latitude")
    private Double latitude;

    @Column(name = "address_longitude")
    private Double longitude;
}