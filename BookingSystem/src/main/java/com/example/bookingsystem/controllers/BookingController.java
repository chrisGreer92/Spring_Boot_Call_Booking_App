package com.example.bookingsystem.controllers;

import com.example.bookingsystem.dtos.BookingDto;
import com.example.bookingsystem.dtos.CreateAvailableSlotDto;
import com.example.bookingsystem.dtos.RequestBookingDto;
import com.example.bookingsystem.dtos.UpdateBookingStatusDto;
import com.example.bookingsystem.entities.Booking;
import com.example.bookingsystem.mappers.BookingMapper;
import com.example.bookingsystem.model.BookingStatus;
import com.example.bookingsystem.repositories.BookingRepository;
import com.example.bookingsystem.services.EmailService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static com.example.bookingsystem.model.BookingStatus.AVAILABLE;
import static com.example.bookingsystem.model.BookingStatus.PENDING;

@RestController
@RequestMapping("/booking")
@AllArgsConstructor
public class BookingController {

    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    private static final Set<String> SORT_FIELDS
            = Set.of("id", "status", "startTime");
    public static final String DEFAULT_SORT = "id";


    /// Maybe refactor to only return admin filter?
    @GetMapping("/public")
    public Iterable<BookingDto> getAvailableBookings(){
        return bookingRepository
                .findFutureFilterStatus(false, AVAILABLE.name())
                .stream().map(bookingMapper::toDto).toList();
    }

    @GetMapping("/admin")
    public Iterable<BookingDto> getAllBookings(
            @RequestParam(required = false, defaultValue = "", name = "sort")
            String sort,

            @RequestParam(required = false, name = "status")
            BookingStatus status,

            @RequestParam(required = false, defaultValue = "false", name = "showDeleted")
            boolean deleted,

            @RequestParam(required = false, defaultValue = "false", name = "showPast")
            boolean showPast

    ){
        if(!SORT_FIELDS.contains(sort)) sort = DEFAULT_SORT;

        List<Booking> bookings;
        if (showPast) {
            bookings = (status != null)
                    ? bookingRepository.findAllByDeletedAndStatus(deleted, status, Sort.by(sort))
                    : bookingRepository.findAllByDeleted(deleted, Sort.by(sort));
        } else {
            bookings = (status != null)
                    //N.B Can't combine showing future only and sorting by something else other than start time
                    //Could implement but wouldn't be as clean, and it's not necessary for API
                    ? bookingRepository.findFutureFilterStatus(deleted, status.name())
                    : bookingRepository.findFuture(deleted);
        }

        return bookings.stream().map(bookingMapper::toDto).toList();
    }

    @PostMapping("/admin")
    public ResponseEntity<Void> createBooking(
            @RequestBody @Valid CreateAvailableSlotDto request
    ){
        var booking = bookingMapper.availableDtoToEntity(request);
        bookingRepository.save(booking);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/request/{id}")
    public ResponseEntity<Void> requestBooking(
            @PathVariable Long id,
            @RequestBody @Valid RequestBookingDto request
    ) {
        var booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return ResponseEntity.notFound().build();
        if (booking.getStatus() != AVAILABLE) return ResponseEntity.status(HttpStatus.CONFLICT).build();

        bookingMapper.applyRequestToBooking(request, booking);
        booking.setStatus(PENDING);
        bookingRepository.save(booking);

        emailService.notifyBookingRequested(booking);

        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/admin/{id}")
    public ResponseEntity<Void> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateBookingStatusDto request
    ){
        var booking = bookingRepository.findById(id).orElse(null);
        if(booking == null) return ResponseEntity.notFound().build();

        booking.setStatus(request.getStatus());
        bookingRepository.save(booking);

        emailService.sendBookingUpdated(booking);

        return ResponseEntity.noContent().build();

    }




    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable(name = "id") Long id
    ){
        var booking = bookingRepository.findById(id).orElse(null);

        if(booking == null) return ResponseEntity.notFound().build();

        bookingRepository.delete(booking);

        return ResponseEntity.noContent().build();
    }
}
