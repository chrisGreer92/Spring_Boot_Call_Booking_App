package com.example.bookingsystem.dtos;

import com.example.bookingsystem.model.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookingStatusDto {

    @NotNull
    private BookingStatus status;

}
