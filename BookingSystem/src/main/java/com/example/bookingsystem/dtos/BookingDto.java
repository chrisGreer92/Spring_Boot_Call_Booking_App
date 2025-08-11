package com.example.bookingsystem.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingDto {

    private Long id;
    private String name;
    private String email;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String topic;
    private String notes;
}
