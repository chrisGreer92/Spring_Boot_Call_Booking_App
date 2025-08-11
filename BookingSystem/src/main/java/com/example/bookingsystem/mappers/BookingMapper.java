package com.example.bookingsystem.mappers;

import com.example.bookingsystem.dtos.BookingDto;
import com.example.bookingsystem.dtos.GenerateBookingDto;
import com.example.bookingsystem.entities.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingDto toDto(Booking booking);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "confirmed", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Booking toEntity(GenerateBookingDto request);
}
