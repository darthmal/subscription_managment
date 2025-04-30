package com.registration.platform.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyContactDTO {

    @NotBlank(message = "Emergency contact name cannot be blank")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Emergency contact relationship cannot be blank")
    @Size(max = 100)
    private String relationship;

    @NotBlank(message = "Emergency contact phone number cannot be blank")
    @Size(max = 30)
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]+$", message = "Invalid phone number format")
    private String phone;
}