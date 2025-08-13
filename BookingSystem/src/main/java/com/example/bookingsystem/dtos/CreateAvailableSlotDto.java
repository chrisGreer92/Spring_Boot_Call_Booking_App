package com.example.bookingsystem.dtos;

import com.example.bookingsystem.validation.ValidTimeRange;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@ValidTimeRange
public class CreateAvailableSlotDto {

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private OffsetDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private OffsetDateTime endTime;

}
