package com.example.bookingsystem.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookingStatusDto {

    @NotNull
    private String status;

}
