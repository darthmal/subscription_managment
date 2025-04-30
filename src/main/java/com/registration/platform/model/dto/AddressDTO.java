package com.registration.platform.model.dto;

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
public class AddressDTO {

    @NotBlank(message = "Street address cannot be blank")
    @Size(max = 255)
    private String street;

    @Size(max = 100)
    private String street2; // Optional

    @NotBlank(message = "City cannot be blank")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "Postal code cannot be blank")
    @Size(max = 20)
    private String postalCode;

    @NotBlank(message = "Country cannot be blank")
    @Size(max = 100)
    private String country;

    // Geolocation fields are usually set by backend processes (if implemented)
    // and might not be part of the request DTO, but could be in the response.
    private Double latitude;
    private Double longitude;
}