package com.example.bookingsystem.validation;

import com.example.bookingsystem.dtos.GenerateBookingDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class TimeRangeValidator implements ConstraintValidator<ValidTimeRange, GenerateBookingDto> {

    @Override
    public boolean isValid(GenerateBookingDto dto, ConstraintValidatorContext context) {
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
