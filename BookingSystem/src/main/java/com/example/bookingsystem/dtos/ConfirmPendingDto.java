package com.example.bookingsystem.dtos;

import com.example.bookingsystem.model.BookingStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class ConfirmPendingDto {

    private String name;
    private String email;
    private String phone;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String topic;
    private BookingStatus status;

}
