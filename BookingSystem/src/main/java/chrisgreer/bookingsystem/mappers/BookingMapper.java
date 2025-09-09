package chrisgreer.bookingsystem.mappers;

import chrisgreer.bookingsystem.dtos.BookingDto;
import chrisgreer.bookingsystem.dtos.CreateAvailableSlotDto;
import chrisgreer.bookingsystem.dtos.RequestBookingDto;
import chrisgreer.bookingsystem.entities.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingDto toDto(Booking booking);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Booking availableDtoToEntity(CreateAvailableSlotDto request);

    void applyRequestToBooking(RequestBookingDto request, @MappingTarget Booking booking);
}
