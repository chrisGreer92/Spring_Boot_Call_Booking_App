package com.example.bookingsystem.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestBookingDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @Pattern(
            regexp = "^[+]?\\d{7,15}$",
            message = "Must be a valid Phone number containing 7–15 digits"
    )
    private String phone;

    private String topic;

    private String notes;

}
