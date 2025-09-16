package chrisgreer.bookingsystem;

import chrisgreer.bookingsystem.dtos.CreateAvailableSlotDto;
import chrisgreer.bookingsystem.dtos.RequestBookingDto;
import chrisgreer.bookingsystem.entities.Booking;
import chrisgreer.bookingsystem.model.BookingStatus;
import chrisgreer.bookingsystem.repositories.BookingRepository;

import java.time.OffsetDateTime;

public class TestUtil {

    public static Booking persistAvailableBooking(BookingRepository bookingRepository){
        Booking booking = createValidBooking();
        bookingRepository.save(booking);
        return booking;
    }

    public static Booking createValidBooking(){
        Booking booking = new Booking();
        booking.setName("Bob");
        booking.setEmail("bob@example.com");
        booking.setStatus(BookingStatus.AVAILABLE);
        booking.setStartTime(OffsetDateTime.now().plusDays(1));
        booking.setEndTime(OffsetDateTime.now().plusDays(1).plusHours(1));
        return booking;
    }

    public static CreateAvailableSlotDto createValidBookingSlot() {
        CreateAvailableSlotDto request = new CreateAvailableSlotDto();
        request.setStartTime(OffsetDateTime.now().plusDays(1));
        request.setEndTime(OffsetDateTime.now().plusDays(1).plusHours(1));
        return request;
    }

    public static RequestBookingDto createValidBookingRequest() {
        RequestBookingDto request = new RequestBookingDto();
        request.setName("Valid Name");
        request.setPhone("01234567890");
        request.setEmail("valid@email.com");
        return request;
    }

}
