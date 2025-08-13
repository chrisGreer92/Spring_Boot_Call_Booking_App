package com.example.bookingsystem.validation;

import com.example.bookingsystem.dtos.CreateAvailableSlotDto;
import com.example.bookingsystem.dtos.RequestBookingDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TimeRangeValidator implements ConstraintValidator<ValidTimeRange, CreateAvailableSlotDto> {

    @Override
    public boolean isValid(CreateAvailableSlotDto dto, ConstraintValidatorContext context) {
        if(dto == null) return true;

        var startTime = dto.getStartTime();
        var endTime = dto.getEndTime();

        if(startTime == null || endTime == null) return true;

        if (!startTime.isBefore(endTime)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Start time must be before end time")
                    .addPropertyNode("startTime")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
