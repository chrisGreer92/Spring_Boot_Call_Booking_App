package com.example.bookingsystem.mappers;

import com.example.bookingsystem.dtos.BookingDto;
import com.example.bookingsystem.dtos.ConfirmedPendingDto;
import com.example.bookingsystem.dtos.CreateAvailableSlotDto;
import com.example.bookingsystem.dtos.RequestBookingDto;
import com.example.bookingsystem.entities.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookingMapper {


    BookingDto toDto(Booking booking);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Booking toEntity(CreateAvailableSlotDto request);

    ConfirmedPendingDto toPendingDto(Booking booking);

    void applyRequestToBooking(RequestBookingDto request, @MappingTarget Booking booking);
}
