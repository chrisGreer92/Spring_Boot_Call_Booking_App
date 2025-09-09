package chrisgreer.bookingsystem.controllers;

import chrisgreer.bookingsystem.dtos.BookingDto;
import chrisgreer.bookingsystem.dtos.CreateAvailableSlotDto;
import chrisgreer.bookingsystem.dtos.RequestBookingDto;
import chrisgreer.bookingsystem.dtos.UpdateBookingStatusDto;
import chrisgreer.bookingsystem.mappers.BookingMapper;
import chrisgreer.bookingsystem.model.BookingStatus;
import chrisgreer.bookingsystem.repositories.BookingRepository;
import chrisgreer.bookingsystem.services.BookingService;
import chrisgreer.bookingsystem.services.EmailService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static chrisgreer.bookingsystem.model.BookingStatus.AVAILABLE;
import static chrisgreer.bookingsystem.model.BookingStatus.PENDING;

@RestController
@RequestMapping("/booking")
@AllArgsConstructor
public class BookingController {

    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    private final BookingService bookingService;


    @GetMapping("/public")
    public Iterable<BookingDto> getAvailableBookings(){
        return bookingService.getAvailableBookings();
    }

    @GetMapping("/admin")
    public Iterable<BookingDto> getBookings(
            @RequestParam(required = false, defaultValue = "", name = "sort")
            String sort,

            @RequestParam(required = false, name = "status")
            BookingStatus status,

            @RequestParam(required = false, defaultValue = "false", name = "showDeleted")
            boolean deleted,

            @RequestParam(required = false, defaultValue = "false", name = "showPast")
            boolean showPast

    ){
        return bookingService.getBookings(sort, status, deleted, showPast);
    }

    @PostMapping("/admin")
    public ResponseEntity<Void> createBooking(
            @RequestBody @Valid CreateAvailableSlotDto dto
    ){
        bookingService.createBooking(dto);
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
        return bookingService.updateBookingStatus(id, request)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }




    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable(name = "id") Long id
    ){
        return bookingService.deleteBooking(id)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.noContent().build();
    }
}
