package com.example.bookingsystem.controllers;

import com.example.bookingsystem.dtos.*;
import com.example.bookingsystem.mappers.BookingMapper;
import com.example.bookingsystem.model.BookingStatus;
import com.example.bookingsystem.repositories.BookingRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;

import static com.example.bookingsystem.model.BookingStatus.*;

@RestController
@RequestMapping("/booking")
@AllArgsConstructor
public class BookingController {

    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;
    private static final Set<String> SORT_FIELDS
            = Set.of("id", "status", "startTime");
    public static final String DEFAULT_SORT = "id";

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(
            @RequestBody @Valid
            CreateAvailableSlotDto request,
            UriComponentsBuilder uriBuilder
    ){

        var booking = bookingMapper.toEntity(request);

        bookingRepository.save(booking);

        var dto = bookingMapper.toDto(booking);
        var uri = uriBuilder.path("/booking/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PatchMapping("/{id}/request")
    public ResponseEntity<ConfirmedPendingDto> requestBooking(
            @PathVariable Long id,
            @RequestBody @Valid RequestBookingDto request
    ) {
        var booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) return ResponseEntity.notFound().build();
        if (booking.getStatus() != AVAILABLE) return ResponseEntity.status(HttpStatus.CONFLICT).build();

        bookingMapper.applyRequestToBooking(request, booking);
        booking.setStatus(PENDING);
        bookingRepository.save(booking);

        var dto = bookingMapper.toPendingDto(booking);

        return ResponseEntity.ok(dto);
    }


    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateBookingStatus(
            @PathVariable Long id,

            @RequestBody @Valid
            UpdateBookingStatusDto request
    ){
        var booking = bookingRepository.findById(id).orElse(null);
        if(booking == null) return ResponseEntity.notFound().build();

        booking.setStatus(request.getStatus());
        bookingRepository.save(booking);

        return ResponseEntity.noContent().build();

    }

    @GetMapping
    public Iterable<BookingDto> getAllBookings(
            @RequestParam(
                    required = false,
                    defaultValue = "",
                    name = "sort")
            String sort,
            @RequestParam(required = false, name = "status")
            BookingStatus status

    ){
        if(!SORT_FIELDS.contains(sort))
            sort = DEFAULT_SORT;

        var bookings = (status != null)
                ? bookingRepository.findAllByStatus(status, Sort.by(sort))
                : bookingRepository.findAll(Sort.by(sort));

        return bookings.stream()
                .map(bookingMapper::toDto)
                .toList();
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(
            @PathVariable(name = "id") Long id
    ){
        var booking = bookingRepository.findById(id).orElse(null);

        if(booking == null) return ResponseEntity.notFound().build();

        bookingRepository.delete(booking);

        return ResponseEntity.noContent().build();
    }
}
