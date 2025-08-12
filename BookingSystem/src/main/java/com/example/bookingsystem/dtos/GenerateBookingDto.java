package com.example.bookingsystem.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class GenerateBookingDto {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

//    @NotBlank (optional)
    @Pattern(regexp = "^[+]?\\d{7,15}$", message = "Invalid phone number")
    private String phone;

    @NotNull
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime startTime;

    @NotNull
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime endTime;

    private String topic;

    private String notes;

}
