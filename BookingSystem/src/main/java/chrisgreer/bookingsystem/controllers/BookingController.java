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
import chrisgreer.bookingsystem.web.ResponseMapper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        return ResponseMapper.toResponse(
                bookingService.requestBooking(id, request)
        );
    }


    @PatchMapping("/admin/{id}")
    public ResponseEntity<Void> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateBookingStatusDto request
    ){

        return ResponseMapper.toResponse(
                bookingService.updateBookingStatus(id, request)
        );
    }




    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable(name = "id") Long id
    ){
        return ResponseMapper.toResponse(
                bookingService.deleteBooking(id)
        );
    }
}
