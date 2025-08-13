package com.example.bookingsystem.dtos;


import com.example.bookingsystem.model.BookingStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static com.example.bookingsystem.model.BookingStatus.PENDING;

@Getter
@Setter
public class BookingDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String topic;
    private String notes;
    private BookingStatus status;

}
