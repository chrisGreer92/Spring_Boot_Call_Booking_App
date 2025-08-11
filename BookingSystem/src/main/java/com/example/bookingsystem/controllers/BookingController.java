package com.example.bookingsystem.controllers;

import com.example.bookingsystem.dtos.BookingDto;
import com.example.bookingsystem.dtos.GenerateBookingDto;
import com.example.bookingsystem.mappers.BookingMapper;
import com.example.bookingsystem.repositories.BookingRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/booking")
public class BookingController {

    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;

    public BookingController(BookingMapper bookingMapper, BookingRepository bookingRepository) {
        this.bookingMapper = bookingMapper;
        this.bookingRepository = bookingRepository;
    }

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(
            @RequestBody @Valid
            GenerateBookingDto request,
            UriComponentsBuilder uriBuilder
    ){

        var booking = bookingMapper.toEntity(request);

        bookingRepository.save(booking);

        var bookingDto = bookingMapper.toDto(booking);
        var uri = uriBuilder.path("/booking/{id}")
                .buildAndExpand(bookingDto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(bookingDto);
    }
}
