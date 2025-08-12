package com.example.bookingsystem.controllers;

import com.example.bookingsystem.dtos.BookingDto;
import com.example.bookingsystem.dtos.GenerateBookingDto;
import com.example.bookingsystem.mappers.BookingMapper;
import com.example.bookingsystem.repositories.BookingRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;

@RestController
@RequestMapping("/booking")
@AllArgsConstructor
public class BookingController {

    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;

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

    @GetMapping
    public Iterable<BookingDto> getAllBookings(
            @RequestParam(
                    required = false,
                    defaultValue = "",
                    name = "sort")
            String sort
    ){

        if(!Set.of("id", "startTime").contains(sort)) //Check if part of our valid list
            sort = "id"; //Default

        return bookingRepository
                .findAll(Sort.by(sort))
                .stream()
                .map(bookingMapper::toDto)
                .toList();
    }
}
