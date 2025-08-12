package com.example.bookingsystem.dtos;

import com.example.bookingsystem.validation.ValidTimeRange;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@ValidTimeRange
public class GenerateBookingDto {

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

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    private String topic;

    private String notes;

}
